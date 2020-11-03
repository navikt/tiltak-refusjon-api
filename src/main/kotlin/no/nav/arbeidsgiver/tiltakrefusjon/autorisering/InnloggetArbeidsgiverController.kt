package no.nav.arbeidsgiver.tiltakrefusjon.autorisering

import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.RefusjonRepository
import no.nav.security.token.support.core.api.Protected
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

const val REQUEST_MAPPING_INNLOGGET_BRUKER = "/api/innloggetBruker"

@RestController
@RequestMapping(REQUEST_MAPPING_INNLOGGET_BRUKER)
@Protected
class InnloggetArbeidsgiverController(val innloggetArbeidsgiverService: InnloggetArbeidsgiverService, val refusjonRepository: RefusjonRepository) {
    @GetMapping
    fun hentInnloggetBruker(): ResponseEntity<InnloggetArbeidsgiver> {
        return ResponseEntity.ok(innloggetArbeidsgiverService.hentInnloggetArbeidsgiver())
    }
}