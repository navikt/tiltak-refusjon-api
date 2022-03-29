package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import no.nav.arbeidsgiver.tiltakrefusjon.UgyldigRequestException
import no.nav.arbeidsgiver.tiltakrefusjon.autorisering.InnloggetBrukerService
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.data.domain.Page
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*


const val REQUEST_MAPPING_ARBEIDSGIVER_REFUSJON = "/api/arbeidsgiver/refusjon"

data class HentArbeidsgiverRefusjonerQueryParametre(
    val bedriftNr: String?,
    val status: RefusjonStatus?,
    val tiltakstype: Tiltakstype?,
    val page: Int = 0,
    val size: Int = 4
)

@RestController
@RequestMapping(REQUEST_MAPPING_ARBEIDSGIVER_REFUSJON)
@ProtectedWithClaims(issuer = "tokenx")
class ArbeidsgiverRefusjonController(
    val innloggetBrukerService: InnloggetBrukerService,
) {
    @GetMapping
    fun hentAlle(queryParametre: HentArbeidsgiverRefusjonerQueryParametre): List<Refusjon> {
        if (queryParametre.bedriftNr == null) {
            throw UgyldigRequestException()
        }
        val arbeidsgiver = innloggetBrukerService.hentInnloggetArbeidsgiver()
        return arbeidsgiver.finnAlleMedBedriftnummer(queryParametre.bedriftNr)
            .filter { queryParametre.status == null || queryParametre.status == it.status }
            .filter { queryParametre.tiltakstype == null || queryParametre.tiltakstype == it.refusjonsgrunnlag.tilskuddsgrunnlag.tiltakstype }
    }

    @GetMapping("/hentliste")
    fun hentListAvBedrifter(queryParametre: HentArbeidsgiverRefusjonerQueryParametre): ResponseEntity<Map<String, Any>> {
        val arbeidsgiver = innloggetBrukerService.hentInnloggetArbeidsgiver()
        val pagableRefusjonlist: Page<Refusjon> = arbeidsgiver.finnAlleForGittArbeidsgiver(
            queryParametre.bedriftNr,
            queryParametre.status,
            queryParametre.tiltakstype,
            queryParametre.page,
            queryParametre.size
        );
        val response = mapOf<String, Any>(
            Pair("refusjoner", pagableRefusjonlist.content),
            Pair("size", pagableRefusjonlist.size),
            Pair("currentPage", pagableRefusjonlist.number),
            Pair("totalItems", pagableRefusjonlist.totalElements),
            Pair("totalPages", pagableRefusjonlist.totalPages)
        )
        return ResponseEntity<Map<String, Any>>(response, HttpStatus.OK)
    }

    @GetMapping("/{id}")
    fun hent(@PathVariable id: String): Refusjon? {
        val arbeidsgiver = innloggetBrukerService.hentInnloggetArbeidsgiver()
        return arbeidsgiver.finnRefusjon(id)
    }

    @PostMapping("/{id}/endre-bruttolønn")
    fun endreBruttolønn(@PathVariable id: String, @RequestBody request: EndreBruttolønnRequest) {
        val arbeidsgiver = innloggetBrukerService.hentInnloggetArbeidsgiver()
         arbeidsgiver.endreBruttolønn(
            id,
            request.inntekterKunFraTiltaket,
            request.bruttoLønn
        )
    }

    @PostMapping("/{id}/fratrekk-sykepenger")
    fun fratrekkSykepenger(@PathVariable id: String, @RequestBody request: FratrekkSykepenger) {
        val arbeidsgiver = innloggetBrukerService.hentInnloggetArbeidsgiver()
        arbeidsgiver.settFratrekkSykepenger(id, request.fratrekkSykepenger, request.sykepengeBeløp)
    }

    @PostMapping("/{id}/set-inntektslinje-opptjent-i-periode")
    fun endreRefundertInntekslinje(@PathVariable id: String, @RequestBody request: EndreRefundertInntektslinjeRequest) {
        val arbeidsgiver = innloggetBrukerService.hentInnloggetArbeidsgiver()
        arbeidsgiver.setInntektslinjeTilOpptjentIPeriode(
            refusjonId = id,
            inntekslinjeId = request.inntektslinjeId,
            erOpptjentIPeriode = request.erOpptjentIPeriode
        )
    }

    @PostMapping("/{id}/godkjenn")
    fun godkjenn(@PathVariable id: String) {
        val arbeidsgiver = innloggetBrukerService.hentInnloggetArbeidsgiver()
        arbeidsgiver.godkjenn(id)
    }
}
