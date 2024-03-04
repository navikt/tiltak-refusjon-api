package no.nav.arbeidsgiver.tiltakrefusjon

import com.jayway.jsonpath.JsonPath
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import no.nav.arbeidsgiver.tiltakrefusjon.audit.AuditLogger
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.events.AuditEntry
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.events.EventType
import no.nav.arbeidsgiver.tiltakrefusjon.utils.Now
import no.nav.security.token.support.core.context.TokenValidationContextHolder
import org.slf4j.LoggerFactory
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import org.springframework.web.util.ContentCachingResponseWrapper
import java.net.URI

/**
 * Dette filteret fanger opp alle responser fra APIet.
 * Dersom en person (arbeidsgiver, saksbehandler) har gjort et oppslag
 * og får returnert en JSON som inneholder "deltakerFnr" så vil dette
 * resultere i en audit-hendelse.
 *
 * Spesifiserer at filteret skal kjøre sist for å garantere at det
 * kjører etter observation-filteret som legger på en traceId.
 */
@Order(Ordered.LOWEST_PRECEDENCE)
@Component
class AuditLoggingFilter(
    val context: TokenValidationContextHolder,
    val auditLogger: AuditLogger
) : OncePerRequestFilter() {
    val log = LoggerFactory.getLogger(javaClass)
    val className = javaClass.name
    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain) {
        val callId: String? = request.getAttribute(CALL_ID_HEADER) as String?
        val wrapper = ContentCachingResponseWrapper(response)
        filterChain.doFilter(request, wrapper)

        if (callId == null) {
            log.error("$className: feilet pga manglende callId. Sjekk om CallIdFilter er riktig satt opp")
        }

        if (response.contentType?.contains("application/json") == true && callId != null) {
            try {
                val brukerId = context.getClaims(Issuer.TOKEN_X)?.getStringClaim("pid") ?: context.getClaims(Issuer.AZURE)?.getStringClaim("NAVident")
                if (brukerId != null && context.erAzureBruker()) {
                    val fnr: List<String> = JsonPath.read<List<String>?>(wrapper.contentInputStream, "$..deltakerFnr").distinct()
                    val utførtTid = Now.instant()

                    val uri = URI.create(request.requestURI)
                    // Logger kun oppslag dersom en innlogget bruker utførte oppslaget
                    fnr.forEach {
                        // Ikke logg at en bruker slår opp sin egen informasjon
                        if (!brukerId.equals(it)) {
                            val entry = AuditEntry(
                                "tiltak-refusjon-api",
                                brukerId,
                                it,
                                EventType.READ,
                                true,
                                utførtTid,
                                msgForUri(uri),
                                uri,
                                request.method,
                                callId
                            )
                            auditLogger.logg(entry)
                        }
                    }
                } else if (brukerId != null && context.erTokenXBruker()) {
                    val fnrOgOrgnr: List<Map<String, String>> = JsonPath.read<List<Map<String, String>>?>(wrapper.contentInputStream, "$..['deltakerFnr', 'bedriftNr']").distinct()
                    val utførtTid = Now.instant()

                    val uri = URI.create(request.requestURI)
                    // Logger kun oppslag dersom en innlogget bruker utførte oppslaget
                    fnrOgOrgnr.forEach {
                        // Ikke logg at en bruker slår opp sin egen informasjon
                        if (it["bedriftNr"] != null && it["deltakerFnr"] != null && brukerId != it["deltakerFnr"]) {
                            val entry = AuditEntry(
                                "tiltak-refusjon-api",
                                brukerId,
                                it["bedriftNr"]!!,
                                EventType.READ,
                                true,
                                utførtTid,
                                msgForUri(uri),
                                uri,
                                request.method,
                                callId
                            )
                            auditLogger.logg(entry)
                        }
                    }
                }
            } catch (ex: Exception) {
                log.error("$className: Logging feilet", ex)
            }
        }
        wrapper.copyBodyToResponse()
    }

    private fun msgForUri(uri: URI): String =
        if (uri.toString().contains("/refusjon/hentliste")) {
            "Oppslag på refusjoner"
        } else if (uri.toString().contains(Regex("/refusjon/\\w+"))) {
            "Hent detaljer om refusjon"
        } else if (uri.toString().contains("/refusjon")) {
            "Oppslag på refusjoner"
        } else if (uri.toString().contains("/korreksjon")) {
            "Oppslag på korreksjoner"
        } else if (uri.toString().contains(Regex("/korreksjon/\\w+"))) {
            "Hent detaljer om korreksjon"
        } else {
            log.warn("${className}: Fant ikke en lesbar melding for uri: $uri")
            "Oppslag i refusjonsløsning"
        }
}
