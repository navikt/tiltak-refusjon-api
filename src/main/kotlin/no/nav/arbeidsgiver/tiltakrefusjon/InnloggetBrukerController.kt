package no.nav.arbeidsgiver.tiltakrefusjon

import no.nav.security.token.support.core.api.Protected
import no.nav.security.token.support.core.context.TokenValidationContextHolder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController



const val REQUEST_MAPPING_INNLOGGET_BRUKER = "/api/innloggetBruker"

@RestController
@RequestMapping(REQUEST_MAPPING_INNLOGGET_BRUKER)
@Protected
class InnloggetBrukerController(val context:TokenValidationContextHolder){

    @GetMapping
    fun hentInnloggetBruker(): InnloggetBruker{
        val brukerFnr:String = context.tokenValidationContext.getClaims("aad").subject
        return InnloggetBruker(brukerFnr, emptyList(), emptyList());
    }

}