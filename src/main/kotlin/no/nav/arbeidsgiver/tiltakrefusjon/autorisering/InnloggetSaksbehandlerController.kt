package no.nav.arbeidsgiver.tiltakrefusjon.autorisering

import no.nav.security.token.support.core.api.Protected
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

const val INNLOGGET_SAKSBEHANDLER_PATH = "/api/innlogget-saksbehandler"

@RestController
@RequestMapping(INNLOGGET_SAKSBEHANDLER_PATH)
@Protected
class InnloggetSaksbehandlerController(val innloggetSaksbehandlerService: InnloggetSaksbehandlerService) {
    @GetMapping
    fun hent(): InnloggetSaksbehandler {
        return innloggetSaksbehandlerService.hentInnloggetSaksbehandler();
    }
}