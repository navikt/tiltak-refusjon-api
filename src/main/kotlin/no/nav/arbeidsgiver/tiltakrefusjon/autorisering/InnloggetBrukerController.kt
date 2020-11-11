package no.nav.arbeidsgiver.tiltakrefusjon.autorisering

import no.nav.security.token.support.core.api.Protected
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

const val REQUEST_MAPPING_INNLOGGET_BRUKER = "/api/innlogget-bruker"

@RestController
@RequestMapping(REQUEST_MAPPING_INNLOGGET_BRUKER)
@Protected
class InnloggetBrukerController(val innloggetBrukerService: InnloggetBrukerService) {
    @GetMapping
    fun hentInnloggetBruker(): InnloggetBruker {
        return innloggetBrukerService.hentInnloggetBruker()
    }
}