package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import no.nav.arbeidsgiver.tiltakrefusjon.autorisering.InnloggetBrukerService
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.events.ForlengFristRequest
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.web.bind.annotation.*

const val REQUEST_MAPPING_SAKSBEHANDLER_REFUSJON = "/api/saksbehandler/refusjon"

data class HentSaksbehandlerRefusjonerQueryParametre(
    val veilederNavIdent: String? = null,
    val enhet: String? = null,
    val deltakerFnr: String? = null,
    val bedriftNr: String? = null,
    val status: RefusjonStatus? = null,
    val tiltakstype: Tiltakstype? = null,
    val avtaleNr: Int? = null,
)

@RestController
@RequestMapping(REQUEST_MAPPING_SAKSBEHANDLER_REFUSJON)
@ProtectedWithClaims(issuer = "aad")
class SaksbehandlerRefusjonController(
    val innloggetBrukerService: InnloggetBrukerService,
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

    @PostMapping("/{id}/korriger")
    fun korriger(@PathVariable id: String, @RequestBody request: KorrigerRequest): Refusjon {
        val saksbehandler = innloggetBrukerService.hentInnloggetSaksbehandler()
        return saksbehandler.korriger(id, request.korreksjonsgrunner)
    }

    @PostMapping("/{id}/endre-bruttolønn")
    fun endreBruttolønn(@PathVariable id: String, @RequestBody request: EndreBruttolønnRequest) {
        val saksbehandler = innloggetBrukerService.hentInnloggetSaksbehandler()
        saksbehandler.endreBruttolønn(id, request.inntekterKunFraTiltaket, request.bruttoLønn)
    }

    @PostMapping("/{id}/slett-korreksjon")
    fun slettKorreksjon(@PathVariable id: String): Refusjon {
        val saksbehandler = innloggetBrukerService.hentInnloggetSaksbehandler()
        return saksbehandler.slettKorreksjon(id)
    }

    @PostMapping("/{id}/forleng-frist")
    fun forlengFrist(@PathVariable id: String, @RequestBody request: ForlengFristRequest): Refusjon {
        val saksbehandler = innloggetBrukerService.hentInnloggetSaksbehandler()
        return saksbehandler.forlengFrist(id, request.nyFrist, request.årsak)
    }
}
