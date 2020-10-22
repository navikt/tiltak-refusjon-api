package no.nav.arbeidsgiver.tiltakrefusjon

import no.nav.security.token.support.core.api.Protected
import no.nav.security.token.support.core.context.TokenValidationContextHolder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

const val INNLOGGET_SAKSBEHANDLER_PATH = "/api/innlogget-saksbehandler"

@RestController
@RequestMapping(INNLOGGET_SAKSBEHANDLER_PATH)
@Protected
class InnloggetSaksbehandlerController(val context: TokenValidationContextHolder) {
    @GetMapping
    fun hent(): InnloggetSaksbehandler {
        val brukerFnr: String = context.tokenValidationContext.getClaims("aad").subject
        return InnloggetSaksbehandler(brukerFnr);
    }
}