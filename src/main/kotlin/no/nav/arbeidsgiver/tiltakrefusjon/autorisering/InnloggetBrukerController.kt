package no.nav.arbeidsgiver.tiltakrefusjon.autorisering

import no.nav.arbeidsgiver.tiltakrefusjon.altinn.AltinnTilgangsstyringService
import no.nav.security.token.support.core.api.Protected
import no.nav.security.token.support.core.context.TokenValidationContextHolder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

const val REQUEST_MAPPING_INNLOGGET_BRUKER = "/api/innloggetBruker"

@RestController
@RequestMapping(REQUEST_MAPPING_INNLOGGET_BRUKER)
@Protected
class InnloggetBrukerController(val context:TokenValidationContextHolder,
                                val altinnTilgangsstyringService: AltinnTilgangsstyringService){
    @GetMapping
    fun hentInnloggetBruker(): InnloggetBruker {
        val valContext = context.tokenValidationContext
        val claims = valContext.getClaims("aad")
        val personIdent = claims.subject
        val orgList = altinnTilgangsstyringService.hentTilganger(personIdent).asList()
        return InnloggetBruker(personIdent, orgList, emptyList());
    }
}