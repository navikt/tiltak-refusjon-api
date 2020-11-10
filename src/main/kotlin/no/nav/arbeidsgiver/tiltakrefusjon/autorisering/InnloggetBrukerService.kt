package no.nav.arbeidsgiver.tiltakrefusjon.autorisering

import no.nav.arbeidsgiver.tiltakrefusjon.altinn.AltinnTilgangsstyringService
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.AbacTilgangsstyringService
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.Fnr
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.Identifikator
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.RefusjonRepository
import no.nav.security.token.support.core.context.TokenValidationContextHolder
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpClientErrorException

@Component
class InnloggetBrukerService(
        val context: TokenValidationContextHolder,
        val graphApiService: GraphApiService,
        val altinnTilgangsstyringService: AltinnTilgangsstyringService,
        val abacTilgangsstyringService: AbacTilgangsstyringService,
        val refusjonRepository: RefusjonRepository
) {
    var logger: Logger = LoggerFactory.getLogger(InnloggetBrukerService::class.java)

    fun hentInnloggetIdent(): Identifikator {
        val valContext = context.tokenValidationContext

        val tokenxClaims = valContext.getClaims("tokenx")
        if (tokenxClaims != null && Fnr.erGyldigFnr(tokenxClaims.getStringClaim("pid"))) {
            return Fnr(tokenxClaims.getStringClaim("pid"))
        }

        val aadClaim = valContext.getClaims("aad")
        if (aadClaim != null) {
            val navIdentClaim = aadClaim.getStringClaim("NAVident")
            return if (navIdentClaim != null) {
                logger.info("NAVident i claim")
                NavIdent(navIdentClaim)
            } else {
                logger.info("Ikke NAVident i claim, henter fra Graph API")
                graphApiService.hentNavIdent()
            }
        }

        throw HttpClientErrorException(HttpStatus.UNAUTHORIZED, "Ukjent token")
    }

    fun hentInnloggetBruker(): InnloggetBruker {
        val ident = hentInnloggetIdent()
        if (ident is Fnr) {
            return InnloggetArbeidsgiver(ident.verdi, altinnTilgangsstyringService, refusjonRepository)
        }
        return InnloggetSaksbehandler(ident.verdi, abacTilgangsstyringService, refusjonRepository)
    }
}