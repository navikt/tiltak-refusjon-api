package no.nav.arbeidsgiver.tiltakrefusjon.audit

import jakarta.servlet.http.HttpServletRequest
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.erGyldigFnr
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.events.AuditEntry
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.events.EventType
import no.nav.arbeidsgiver.tiltakrefusjon.utils.Issuer
import no.nav.arbeidsgiver.tiltakrefusjon.utils.Now
import no.nav.arbeidsgiver.tiltakrefusjon.utils.getClaims
import no.nav.security.token.support.core.context.TokenValidationContextHolder
import org.aspectj.lang.JoinPoint
import org.aspectj.lang.annotation.AfterReturning
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.reflect.MethodSignature
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import java.net.URI

@Aspect
@Component
class AuditLoggingAspect(val context: TokenValidationContextHolder, val auditLogger: AuditLogger) {
    var log: Logger = LoggerFactory.getLogger(javaClass)

    /**
     * Denne "handleren" kjøres etter at en controller-metode er ferdigkjørt, og brukes for å se om verdien som returneres
     * er en eller flere refusjoner som kan logges. Hvis det er tilfellet, logges det et audit-event for hver unike kombinasjon
     * av deltaker/bedrift.
     *
     * @param joinPoint            Dette er punktet som denne handleren "henger" på. Brukes for å hente ut annotasjonsbeskrivelsen
     * @param resultatFraEndepunkt Objektet som ble returnert av controller-metoden
     */
    @AfterReturning(value = "@annotation(no.nav.arbeidsgiver.tiltakrefusjon.audit.AuditLogging)", returning = "resultatFraEndepunkt")
    fun postProcess(joinPoint: JoinPoint, resultatFraEndepunkt: Any) {
        val httpServletRequest = (RequestContextHolder.currentRequestAttributes() as ServletRequestAttributes).request;

        sendAuditmeldingerTilKafka(httpServletRequest, hentAuditLoggingAnnotasjonsverdi(joinPoint), hentEntiteterSomKanAuditlogges(resultatFraEndepunkt));
    }

    /**
     * På grunn av at Collection, HashMap og ResponseEntity er generics, er vi nødt til å kverne igjennom mange
     * instanceof-sjekker for å finne ut om responsen fra controller-metoden som wrappes av Auditlogging-annotasjonen
     * faktisk inneholder en "auditerbar" avtale.
     * <br/>
     * Hvis returverdien er en ResponseEntity eller HashMap, så "unboxer" vi disse og kaller funksjonen igjen.
     * I tilfellet hvor objektet er et HashMap prøver vi å hente ut refusjoner fra "refusjoner"-nøkkelen.
     */
    private fun hentEntiteterSomKanAuditlogges(resultatobjekt: Any?): Set<FnrOgBedrift> {
        if (resultatobjekt is ResponseEntity<*>) {
            // Rekursivt kall for å "unboxe" ResponseEntity
            return hentEntiteterSomKanAuditlogges(resultatobjekt.body)
        } else if (resultatobjekt is Map<*, *>) {
            // Rekursivt kall for å "unboxe" HashMap (vil sannsynligvis da treffe Collection-branchen under)
            return hentEntiteterSomKanAuditlogges(resultatobjekt["refusjoner"])
        }

        val entiteter = ArrayList<RefusjonMedFnrOgBedrift>()
        if (resultatobjekt is Collection<*>) {
            resultatobjekt.forEach { refusjon ->
                if (refusjon is RefusjonMedFnrOgBedrift) {
                    entiteter.add(refusjon)
                }
            }
            if (resultatobjekt.size != entiteter.size) {
                log.error(
                    "AuditLoggingAspect fant en respons som ikke inneholdt refusjoner: {}", resultatobjekt.first()?.javaClass?.name ?: "null"
                )
            }
        } else if (resultatobjekt is RefusjonMedFnrOgBedrift) {
            // Responsen var en enkelt auditentitet
            entiteter.add(resultatobjekt)
        } else {
            log.error("AuditLoggingAspect støtter ikke denne typen responsobjekt: {}", resultatobjekt?.javaClass?.name ?: "null")
        }
        return hentOppslagsdata(entiteter)
    }

    private fun hentAuditLoggingAnnotasjonsverdi(joinPoint: JoinPoint): String {
        val methodSignature = joinPoint.signature as MethodSignature
        return methodSignature.method.getAnnotation(AuditLogging::class.java).value
    }

    /**
     * Konverterer auditerbare refusjoner til et FnrOgBedrift-sett for å sikre at vi får ut unike
     * oppslag (hvis vi ikke gjør dette vil man feks logge oppslag mot samme deltaker i to refusjoner dobbelt).
     */
    private fun hentOppslagsdata(result: Collection<RefusjonMedFnrOgBedrift>): Set<FnrOgBedrift> {
        return result.map {
            it.getFnrOgBedrift()
        }.toSet()
    }

    private fun sendAuditmeldingerTilKafka(request: HttpServletRequest, apiBeskrivelse: String, auditElementer: Set<FnrOgBedrift>) {
        try {
            val innloggetBrukerId = context.getClaims(Issuer.TOKEN_X)?.getStringClaim("pid") ?: context.getClaims(Issuer.AZURE)?.getStringClaim("NAVident")
            // Logger kun oppslag dersom en innlogget bruker utførte oppslaget
            if (innloggetBrukerId != null) {
                val uri = URI.create(request.requestURI)
                val utførtTid = Now.instant()

                val innloggetBrukerErPrivatperson = erGyldigFnr(innloggetBrukerId)
                auditElementer.forEach { fnrOgBedrift ->
                    // Vi er ikke interessert i oppslag som bruker gjør på seg selv
                    if (fnrOgBedrift.deltakerFnr.equals(innloggetBrukerId)) {
                        return
                    }
                    // Traceparent header-format: https://www.w3.org/TR/trace-context/#traceparent-header
                    // Eksempel: 00-4bf92f3577b34da6a3ce929d0e0e4736-00f067aa0ba902b7-01
                    // Vi er interessert i del 2.
                    val traceHeader: String? = request.getHeader("traceparent")?.split("-")?.getOrNull(1)
                    if (traceHeader == null) {
                        log.error("traceparent header mangler i request!")
                    }
                    auditLogger.logg(
                        AuditEntry(
                            appNavn = "tiltak-refusjon-api",
                            // ArcSight vil ikke ha oppslag som er utført av en privatperson; oppslaget må derfor være "utført av" en bedrift
                            utførtAv = if (innloggetBrukerErPrivatperson) fnrOgBedrift.bedrift else innloggetBrukerId,
                            oppslagPå = fnrOgBedrift.deltakerFnr,
                            eventType = EventType.READ,
                            forespørselTillatt = true,
                            oppslagUtførtTid = utførtTid,
                            beskrivelse = apiBeskrivelse,
                            requestUrl = uri,
                            requestMethod = request.method,
                            correlationId = traceHeader ?: ""
                        )
                    )
                }
            }
        } catch (ex: Exception) {
            log.error("{}: Logging feilet", javaClass.name, ex);
        }
    }
}
