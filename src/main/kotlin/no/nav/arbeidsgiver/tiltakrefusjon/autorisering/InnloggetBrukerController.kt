package no.nav.arbeidsgiver.tiltakrefusjon.autorisering

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
class InnloggetBrukerController(val innloggingService: InnloggingService) {
    @GetMapping
    fun hentInnloggetBruker(): ResponseEntity<InnloggetBruker> {
        return ResponseEntity.ok(innloggingService.hentInnloggetBruker());
    }

    @GetMapping("/identifikator")
    fun hentIdentifikator(): Identifikator {
        return innloggingService.hentPaloggetIdent();
    }
}