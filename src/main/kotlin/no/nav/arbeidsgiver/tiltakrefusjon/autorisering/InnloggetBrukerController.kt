package no.nav.arbeidsgiver.tiltakrefusjon.autorisering

import no.nav.arbeidsgiver.tiltakrefusjon.altinn.AltinnTilgangsstyringService
import no.nav.arbeidsgiver.tiltakrefusjon.altinn.Organisasjon
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.Fnr
import no.nav.security.token.support.core.api.Protected
import no.nav.security.token.support.core.context.TokenValidationContextHolder
import org.springframework.http.ResponseEntity
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
    fun hentInnloggetBruker(): ResponseEntity<InnloggetBruker> {
        val valContext = context.tokenValidationContext
        val claims = valContext.getClaims("tokenx")
        val personIdent = Fnr(claims.get("pid").toString())
        val organisasjoner:Set<Organisasjon> = altinnTilgangsstyringService.hentTilganger(personIdent)
        return ResponseEntity.ok(InnloggetBruker(personIdent.verdi, organisasjoner));
    }
}