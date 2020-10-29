package no.nav.arbeidsgiver.tiltakrefusjon.autorisering

import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.Fnr
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.Identifikator
import no.nav.security.token.support.core.context.TokenValidationContextHolder
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpClientErrorException

@Component
class InnloggetBrukerService(val context: TokenValidationContextHolder) {

    fun hentInnloggetIdent(): Identifikator {
        return hentPaloggetIdent()
    }

    private fun hentPaloggetIdent(): Identifikator {
        val valContext = context.tokenValidationContext

        val tokenxClaims = valContext.getClaims("tokenx")
        if (tokenxClaims != null && Fnr.erGyldigFnr(tokenxClaims.getStringClaim("pid"))) {
            return Fnr(tokenxClaims.getStringClaim("pid"))
        }

        val aadClaim = valContext.getClaims("aad")
        if (aadClaim != null && NavIdent.erNavIdent(aadClaim.subject)) {
            return NavIdent(aadClaim.subject)
        }
        throw HttpClientErrorException(HttpStatus.UNAUTHORIZED, "Ukjent token")
    }
}