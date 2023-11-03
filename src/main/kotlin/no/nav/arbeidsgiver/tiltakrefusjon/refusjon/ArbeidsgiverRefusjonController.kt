package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import no.nav.arbeidsgiver.tiltakrefusjon.UgyldigRequestException
import no.nav.arbeidsgiver.tiltakrefusjon.autorisering.InnloggetBrukerService
import no.nav.arbeidsgiver.tiltakrefusjon.dokgen.DokgenService
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.data.domain.Page
import org.springframework.http.*
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*
import java.time.Instant

const val REQUEST_MAPPING_ARBEIDSGIVER_REFUSJON = "/api/arbeidsgiver/refusjon"

data class HentArbeidsgiverRefusjonerQueryParametre(
    val bedriftNr: String?,
    val status: RefusjonStatus?,
    val tiltakstype: Tiltakstype?,
    val sorting: SortingOrder?,
    val page: Int = 0,
    val size: Int = 10
)

@RestController
@RequestMapping(REQUEST_MAPPING_ARBEIDSGIVER_REFUSJON)
@ProtectedWithClaims(issuer = "tokenx")
class ArbeidsgiverRefusjonController(
    val innloggetBrukerService: InnloggetBrukerService,
    val dokgenService: DokgenService
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

    @GetMapping("/{id}/pdf")
    fun hentPDF(@PathVariable id:String): HttpEntity<ByteArray>{
        if(id.trim().isEmpty()) return HttpEntity.EMPTY as HttpEntity<ByteArray>
        val arbeidsgiver = innloggetBrukerService.hentInnloggetArbeidsgiver()
        val refusjon = arbeidsgiver.finnRefusjon(id)
        val pdfDataAsByteArray: ByteArray = dokgenService.refusjonPdf(refusjon)

        val header = HttpHeaders()
        header.contentType = MediaType.APPLICATION_PDF
        header[HttpHeaders.CONTENT_DISPOSITION] = "inline; filename=Refusjon om " + refusjon.refusjonsgrunnlag.tilskuddsgrunnlag.tiltakstype.name + ".pdf"
        header.contentLength = pdfDataAsByteArray.size.toLong()
        return HttpEntity<ByteArray>(pdfDataAsByteArray, header)

    }

    @GetMapping("/hentliste")
    fun hentListAvBedrifter(queryParametre: HentArbeidsgiverRefusjonerQueryParametre): ResponseEntity<Map<String, Any>> {
        val arbeidsgiver = innloggetBrukerService.hentInnloggetArbeidsgiver()
        val pagableRefusjonlist: Page<Refusjon> = arbeidsgiver.finnAlleForGittArbeidsgiver(
            queryParametre.bedriftNr,
            queryParametre.status,
            queryParametre.tiltakstype,
            queryParametre.sorting,
            queryParametre.page,
            queryParametre.size
        );
        val response = mapOf(
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

    @PostMapping("{id}/oppdater-refusjon")
    fun oppdaterRefusjon(@PathVariable id: String, @RequestHeader(HttpHeaders.IF_UNMODIFIED_SINCE) sistEndret: Instant?) {
        val arbeidsgiver = innloggetBrukerService.hentInnloggetArbeidsgiver()
        arbeidsgiver.oppdaterRefusjon(id, sistEndret);
    }

    @PostMapping("/{id}/endre-bruttolønn")
    @Transactional
    fun endreBruttolønn(@PathVariable id: String, @RequestBody request: EndreBruttolønnRequest, @RequestHeader(HttpHeaders.IF_UNMODIFIED_SINCE) sistEndret: Instant) {
        val arbeidsgiver = innloggetBrukerService.hentInnloggetArbeidsgiver()
         arbeidsgiver.endreBruttolønn(
            id,
            request.inntekterKunFraTiltaket,
            request.bruttoLønn,
            sistEndret
        )
    }

    @PostMapping("/{id}/lagre-bedriftKID")
    @Transactional
    fun lagreBedriftKID(@PathVariable id: String, @RequestBody request: EndreBedriftKIDRequest, @RequestHeader(HttpHeaders.IF_UNMODIFIED_SINCE) sistEndret: Instant) {
        val arbeidsgiver = innloggetBrukerService.hentInnloggetArbeidsgiver()
        arbeidsgiver.lagreBedriftKID(id, request.bedriftKID)
    }

    @PostMapping("/{id}/fratrekk-sykepenger")
    @Transactional
    fun fratrekkSykepenger(@PathVariable id: String, @RequestBody request: FratrekkRefunderbarBeløp, @RequestHeader(HttpHeaders.IF_UNMODIFIED_SINCE) sistEndret: Instant) {
        val arbeidsgiver = innloggetBrukerService.hentInnloggetArbeidsgiver()
        arbeidsgiver.settFratrekkRefunderbarBeløp(id, request.fratrekkRefunderbarBeløp, request.refunderbarBeløp, sistEndret)
    }

    @PostMapping("/{id}/set-inntektslinje-opptjent-i-periode")
    @Transactional
    fun endreRefundertInntekslinje(@PathVariable id: String, @RequestBody request: EndreRefundertInntektslinjeRequest, @RequestHeader(HttpHeaders.IF_UNMODIFIED_SINCE) sistEndret: Instant) {
        val arbeidsgiver = innloggetBrukerService.hentInnloggetArbeidsgiver()
        arbeidsgiver.setInntektslinjeTilOpptjentIPeriode(
            refusjonId = id,
            inntekslinjeId = request.inntektslinjeId,
            erOpptjentIPeriode = request.erOpptjentIPeriode,
            sistEndret
        )
    }

    @PostMapping("/{id}/godkjenn")
    @Transactional
    fun godkjenn(@PathVariable id: String, @RequestHeader(HttpHeaders.IF_UNMODIFIED_SINCE) sistEndret: Instant) {
        val arbeidsgiver = innloggetBrukerService.hentInnloggetArbeidsgiver()
        arbeidsgiver.godkjenn(id, sistEndret)
    }

    @PostMapping("/{id}/godkjenn-nullbeløp")
    @Transactional
    fun godkjennNullbeløp(@PathVariable id: String, @RequestHeader(HttpHeaders.IF_UNMODIFIED_SINCE) sistEndret: Instant) {
        val arbeidsgiver = innloggetBrukerService.hentInnloggetArbeidsgiver()
        arbeidsgiver.godkjennNullbeløp(id, sistEndret)
    }

    @PostMapping("/{id}/merk-for-hent-inntekter-frem")
    @Transactional
    fun merkForHentInntekterFrem(@PathVariable id: String, @RequestBody request: MerkInntekterFremRequest, @RequestHeader(HttpHeaders.IF_UNMODIFIED_SINCE) sistEndret: Instant) {
        val arbeidsgiver = innloggetBrukerService.hentInnloggetArbeidsgiver()
        arbeidsgiver.merkForHentInntekterFrem(id, request.merking)
    }
}
