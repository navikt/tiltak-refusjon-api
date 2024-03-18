package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import no.nav.arbeidsgiver.tiltakrefusjon.ReberegnRequest
import no.nav.arbeidsgiver.tiltakrefusjon.autorisering.InnloggetBrukerService
import no.nav.arbeidsgiver.tiltakrefusjon.hendelseslogg.HendelsesloggDTO
import no.nav.arbeidsgiver.tiltakrefusjon.hendelseslogg.HendelsesloggRepository
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
    val page: Int = 0,
    val size: Int = 10
)

data class MerkForUnntakOmInntekterToMånederFremRequest(val merking: Int)

@RestController
@RequestMapping(REQUEST_MAPPING_SAKSBEHANDLER_REFUSJON)
@ProtectedWithClaims(issuer = "aad")
class SaksbehandlerRefusjonController(
    val innloggetBrukerService: InnloggetBrukerService,
    val hendelsesloggRepository: HendelsesloggRepository,
) {
    @GetMapping
    fun hentAlle(queryParametre: HentSaksbehandlerRefusjonerQueryParametre): Map<String, Any> {
        val saksbehandler = innloggetBrukerService.hentInnloggetSaksbehandler()
        return saksbehandler.finnAlle(queryParametre)
    }

    @GetMapping("/{id}")
    fun hent(@PathVariable id: String): Refusjon? {
        val saksbehandler = innloggetBrukerService.hentInnloggetSaksbehandler()
        return saksbehandler.finnRefusjon(id)
    }

    @GetMapping("/{id}/hendelselogg")
    fun hentHendelselogg(@PathVariable id: String): List<HendelsesloggDTO> {
        val refusjon = innloggetBrukerService.hentInnloggetSaksbehandler().finnRefusjon(id)
        val hendelser = hendelsesloggRepository.findAllByRefusjonId(refusjon.id)
        return hendelser.map { HendelsesloggDTO(it) }
    }

    @PostMapping("/{id}/forleng-frist")
    fun forlengFrist(@PathVariable id: String, @RequestBody request: ForlengFristRequest): Refusjon {
        val saksbehandler = innloggetBrukerService.hentInnloggetSaksbehandler()
        return saksbehandler.forlengFrist(id, request.nyFrist, request.årsak)
    }

    @PostMapping("/{id}/merk-for-unntak-om-inntekter-to-mnd-frem")
    fun merkForUnntakOmInntekterToMånederFrem(@PathVariable id: String, @RequestBody request: MerkForUnntakOmInntekterToMånederFremRequest) {
        val saksbehandler = innloggetBrukerService.hentInnloggetSaksbehandler()
        saksbehandler.merkForUnntakOmInntekterFremITid(id, request.merking)
    }

    @PostMapping("/{id}/merk-for-unntak-om-inntekter-frem-i-tid")
    fun merkForUnntakOmInntekterFremITid(@PathVariable id: String, @RequestBody request: MerkForUnntakOmInntekterToMånederFremRequest) {
        val saksbehandler = innloggetBrukerService.hentInnloggetSaksbehandler()
        saksbehandler.merkForUnntakOmInntekterFremITid(id, request.merking)
    }


    @PostMapping("reberegn-dry/{id}")
    fun reberegnDryRun(@PathVariable id: String, @RequestBody request: ReberegnRequest): Beregning {
        val saksbehandler = innloggetBrukerService.hentInnloggetSaksbehandler()
        return saksbehandler.reberegnDryRun(id, request.harFerietrekkForSammeMåned, request.minusBeløp)
    }
}
