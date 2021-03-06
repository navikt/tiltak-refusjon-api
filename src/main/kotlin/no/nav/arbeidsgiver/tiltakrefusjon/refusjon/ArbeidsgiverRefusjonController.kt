package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import no.nav.arbeidsgiver.tiltakrefusjon.UgyldigRequestException
import no.nav.arbeidsgiver.tiltakrefusjon.autorisering.InnloggetBrukerService
import no.nav.security.token.support.core.api.Protected
import org.springframework.web.bind.annotation.*

const val REQUEST_MAPPING_ARBEIDSGIVER_REFUSJON = "/api/arbeidsgiver/refusjon"

data class HentArbeidsgiverRefusjonerQueryParametre(val bedriftNr: String?, val status: RefusjonStatus?, val tiltakstype: Tiltakstype?)

@RestController
@RequestMapping(REQUEST_MAPPING_ARBEIDSGIVER_REFUSJON)
@Protected
class ArbeidsgiverRefusjonController(
        val innloggetBrukerService: InnloggetBrukerService
) {
    @GetMapping
    fun hentAlle(queryParametre: HentArbeidsgiverRefusjonerQueryParametre): List<Refusjon> {
        if (queryParametre.bedriftNr == null) {
            throw UgyldigRequestException()
        }
        val arbeidsgiver = innloggetBrukerService.hentInnloggetArbeidsgiver()
        return arbeidsgiver.finnAlleMedBedriftnummer(queryParametre.bedriftNr)
                .filter { queryParametre.status == null || queryParametre.status == it.status }
                .filter { queryParametre.tiltakstype == null || queryParametre.tiltakstype == it.tilskuddsgrunnlag.tiltakstype }
    }

    @GetMapping("/{refusjonId}/tidligere-refusjoner")
    fun hentTidligereRefusjoner(@PathVariable refusjonId: String): List<Refusjon> {
        val arbeidsgiver = innloggetBrukerService.hentInnloggetArbeidsgiver()
        return arbeidsgiver.finnTidligereRefusjoner(refusjonId)
    }

    @GetMapping("/{id}")
    fun hent(@PathVariable id: String): Refusjon? {
        val arbeidsgiver = innloggetBrukerService.hentInnloggetArbeidsgiver()
        return arbeidsgiver.finnRefusjon(id)
    }

    @PostMapping("/{id}/inntektsoppslag")
    fun gjørInntektsoppslag(@PathVariable id: String) {
        val arbeidsgiver = innloggetBrukerService.hentInnloggetArbeidsgiver()
        arbeidsgiver.gjørInntektsoppslag(id)
    }

    @PostMapping("/{id}/godkjenn")
    fun godkjenn(@PathVariable id: String) {
        val arbeidsgiver = innloggetBrukerService.hentInnloggetArbeidsgiver()
        arbeidsgiver.godkjenn(id)
    }
}
