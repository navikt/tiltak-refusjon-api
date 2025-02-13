package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import no.nav.arbeidsgiver.tiltakrefusjon.UgyldigRequestException
import no.nav.arbeidsgiver.tiltakrefusjon.audit.AuditLogging
import no.nav.arbeidsgiver.tiltakrefusjon.autorisering.InnloggetBrukerService
import no.nav.arbeidsgiver.tiltakrefusjon.dokgen.DokgenService
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoField

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
    var logger: Logger = LoggerFactory.getLogger(javaClass)

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

    @AuditLogging("Hent detaljer om en refusjon")
    @GetMapping("/{id}")
    fun hent(@PathVariable id: String): Refusjon? {
        val arbeidsgiver = innloggetBrukerService.hentInnloggetArbeidsgiver()
        return arbeidsgiver.finnRefusjon(id)
    }

    @PostMapping("{id}/sett-kontonummer-og-inntekter")
    @Transactional
    fun settKontonummerOgInntekterPåRefusjon(@PathVariable id: String, @RequestHeader(HttpHeaders.IF_UNMODIFIED_SINCE) sistEndret: Instant?) {
        val arbeidsgiver = innloggetBrukerService.hentInnloggetArbeidsgiver()
        arbeidsgiver.settKontonummerOgInntekterPåRefusjon(id, sistEndret);
    }

    @PostMapping("{id}/sett-kontonummer-og-inntekter", consumes = ["application/json"])
    @Transactional
    fun settKontonummerOgInntekterPåRefusjon(@PathVariable id: String, @RequestHeader(HttpHeaders.IF_UNMODIFIED_SINCE) sistEndret: Instant?, @RequestBody body: SistEndretBody?) {
        if (body?.sistEndret != null && sistEndret != null && Duration.between(sistEndret, body.sistEndret).toMinutes() > 1) {
            val avvik = Duration.between(sistEndret, body.sistEndret).toMinutes() > 1
            logger.warn("SistEndret-tid i body og header divergerer for refusjon $id med $avvik minutter")
        }
        val arbeidsgiver = innloggetBrukerService.hentInnloggetArbeidsgiver()
        arbeidsgiver.settKontonummerOgInntekterPåRefusjon(id, sistEndret)
    }

    data class SistEndretBody(val sistEndret: Instant?)

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
        arbeidsgiver.lagreBedriftKID(id, request.bedriftKID, sistEndret)
    }

    @PostMapping("/{id}/fratrekk-sykepenger")
    @Transactional
    fun fratrekkSykepenger(@PathVariable id: String, @RequestBody request: FratrekkRefunderbarBeløp, @RequestHeader(HttpHeaders.IF_UNMODIFIED_SINCE) sistEndret: Instant) {
        val arbeidsgiver = innloggetBrukerService.hentInnloggetArbeidsgiver()
        arbeidsgiver.settFratrekkRefunderbarBeløp(id, request.fratrekkRefunderbarBeløp, request.refunderbarBeløp, sistEndret)
    }

    @PostMapping("/{id}/set-inntektslinje-opptjent-i-periode")
    @Transactional
    fun settInntektslinjeOpptjentIPeriode(@PathVariable id: String, @RequestBody request: EndreRefundertInntektslinjeRequest, @RequestHeader(HttpHeaders.IF_UNMODIFIED_SINCE) sistEndret: Instant) {
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
        arbeidsgiver.merkForHentInntekterFrem(id, request.merking, sistEndret)
    }
}
