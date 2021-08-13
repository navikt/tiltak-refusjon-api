package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import no.nav.arbeidsgiver.tiltakrefusjon.autorisering.InnloggetBrukerService
import no.nav.security.token.support.core.api.Protected
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

const val REQUEST_MAPPING_SAKSBEHANDLER_REFUSJON = "/api/saksbehandler/refusjon"

data class HentSaksbehandlerRefusjonerQueryParametre(val veilederNavIdent: String? = null, val deltakerFnr: String? = null, val bedriftNr: String? = null, val enhet: String? = null, val refusjonId: String? = null, val status: RefusjonStatus? = null, val tiltakstype: Tiltakstype? = null)

@RestController
@RequestMapping(REQUEST_MAPPING_SAKSBEHANDLER_REFUSJON)
@Protected
class SaksbehandlerRefusjonController(
        val innloggetBrukerService: InnloggetBrukerService
) {
    @GetMapping
    fun hentAlle(queryParametre: HentSaksbehandlerRefusjonerQueryParametre): List<Refusjon> {
        val saksbehandler = innloggetBrukerService.hentInnloggetSaksbehandler()
        return saksbehandler.finnAlle(queryParametre)
    }

    @GetMapping("/{id}")
    fun hent(@PathVariable id: String): Refusjon? {
        val saksbehandler = innloggetBrukerService.hentInnloggetSaksbehandler()
        return saksbehandler.finnRefusjon(id)
    }
}
