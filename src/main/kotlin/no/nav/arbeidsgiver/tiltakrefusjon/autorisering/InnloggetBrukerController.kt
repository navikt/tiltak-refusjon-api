package no.nav.arbeidsgiver.tiltakrefusjon.autorisering

import no.nav.arbeidsgiver.tiltakrefusjon.altinn.Organisasjon
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.Identifikator
import no.nav.security.token.support.core.api.Protected
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

const val REQUEST_MAPPING_INNLOGGET_BRUKER = "/api/innloggetBruker"

@RestController
@RequestMapping(REQUEST_MAPPING_INNLOGGET_BRUKER)
@Protected
class InnloggetBrukerController(val innloggingService: InnloggingService){
    @GetMapping
    fun hentInnloggetBruker(): ResponseEntity<InnloggetBruker> {
        val paloggetFnr:Identifikator = innloggingService.hentPaloggetIdent()
        val organisasjoner:Set<Organisasjon> = innloggingService.hentTilganger(paloggetFnr)
        return ResponseEntity.ok(InnloggetBruker(paloggetFnr.verdi, organisasjoner));
    }
}