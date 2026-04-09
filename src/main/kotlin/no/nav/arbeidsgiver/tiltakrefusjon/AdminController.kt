package no.nav.arbeidsgiver.tiltakrefusjon

import no.nav.arbeidsgiver.tiltakrefusjon.automatisk_utbetaling.AutomatiskInnsendingService
import no.nav.arbeidsgiver.tiltakrefusjon.autorisering.ADMIN_BRUKER
import no.nav.arbeidsgiver.tiltakrefusjon.grunnbelop.GrunnbelopService
import no.nav.arbeidsgiver.tiltakrefusjon.leader.LeaderPodCheck
import no.nav.arbeidsgiver.tiltakrefusjon.okonomi.KontoregisterServiceImpl
import no.nav.arbeidsgiver.tiltakrefusjon.rapport.UbetaltRefusjonRapport
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.Beregning
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.Beregningskontekst
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.Korreksjonsgrunn
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.Refusjon
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.RefusjonKafkaProducer
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.RefusjonRepository
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.RefusjonService
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.RefusjonStatus
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.StatusJobb
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.beregnRefusjonsbeløp
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.events.RefusjonEndretStatus
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.events.RefusjonUtgått
import no.nav.arbeidsgiver.tiltakrefusjon.tilskuddsperiode.MidlerFrigjortÅrsak
import no.nav.arbeidsgiver.tiltakrefusjon.tilskuddsperiode.TilskuddsperiodeForkortetMelding
import no.nav.arbeidsgiver.tiltakrefusjon.tilskuddsperiode.TilskuddsperiodeGodkjentMelding
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.ok
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

@ProtectedWithClaims(issuer = "azure-access-token", claimMap = ["groups=fb516b74-0f2e-4b62-bad8-d70b82c3ae0b"])
@RestController
@RequestMapping("/internal/admin")
class AdminController(
    private val service: RefusjonService,
    private val refusjonRepository: RefusjonRepository,
    private val refusjonService: RefusjonService,
    private val refusjonKafkaProducer: RefusjonKafkaProducer?,
    private val kontoregisterService: KontoregisterServiceImpl?,
    private val automatiskInnsendingService: AutomatiskInnsendingService,
    private val ubetaltRefusjonRapport: UbetaltRefusjonRapport,
    private val grunnbelopService: GrunnbelopService,
    private val statusJobb: StatusJobb,
) {
    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    @GetMapping("kontoregister/{orgnr}")
    fun kontoregisterKall(@PathVariable orgnr: String): String {
        return "Bank kontonummer: " + kontoregisterService?.hentBankkontonummer(orgnr)
    }

    @GetMapping("/")
    fun hjem(): String? {
        return "Velkommen til Refusjon Admin API"
    }

    @PostMapping("opprett-refusjon")
    fun opprettRefusjon(@RequestBody jsonMelding: TilskuddsperiodeGodkjentMelding): Refusjon? {
        logger.info(
            "Bruker AdminController for å opprette refusjon med tilskuddsperiodeId {}",
            jsonMelding.tilskuddsperiodeId
        )
        return service.opprettRefusjon(jsonMelding)
    }

    @PostMapping("opprett-refusjoner")
    fun opprettRefusjoner(@RequestBody jsonMeldinger: List<TilskuddsperiodeGodkjentMelding>) {
        jsonMeldinger.forEach {
            opprettRefusjon(it)
        }
    }

    @PostMapping("forkort-tilskuddsperiode")
    fun forkortTilskuddsperiode(@RequestBody jsonMelding: TilskuddsperiodeForkortetMelding) {
        logger.info(
            "Bruker AdminController for å forkorte tilskuddsperiode med tilskuddsperiodeId {}",
            jsonMelding.tilskuddsperiodeId
        )
        service.forkortRefusjon(jsonMelding)
    }

    @PostMapping("lag-korreksjoner")
    fun lagKorreksjoner(@RequestBody korreksjonRequest: KorreksjonRequest): List<String> {
        logger.info(
            "Bruker AdminController for å opprette korreksjon på {} refusjoner",
            korreksjonRequest.refusjonIder.size
        )
        val korreksjoner = mutableListOf<String>()
        for (id in korreksjonRequest.refusjonIder) {
            val refusjon =
                refusjonRepository.findByIdOrNull(id) ?: throw RuntimeException("Finner ikke refusjon med id=$id")
            service.opprettKorreksjonsutkast(refusjon, korreksjonRequest.korreksjonsgrunner, 2, annetGrunn = null)
            // korreksjoner.add(korreksjon.id)
        }
        return korreksjoner
    }

    @PostMapping("forleng-frister")
    fun forlengFrister(@RequestBody request: ForlengFristerRequest) {
        logger.info(
            "Bruker AdminController for å forlenge frister på {} refusjoner",
            request.refusjonIder.size
        )
        for (id in request.refusjonIder) {
            val refusjon =
                refusjonRepository.findByIdOrNull(id) ?: throw RuntimeException("Finner ikke refusjon med id=$id")
            try {
                refusjon.forlengFrist(request.nyFrist, request.årsak, ADMIN_BRUKER, request.tillatForlengingUtoverMaksimalFrist)
                refusjonRepository.save(refusjon)
            } catch (e: FeilkodeException) {
                if (e.feilkode == Feilkode.FOR_LANG_FORLENGELSE_AV_FRIST) {
                    logger.warn("Forlengelse av frist på refusjon med id=$id overskrider grensen på 1 måned")
                } else {
                    throw e
                }
            }
        }
    }

    @PostMapping("annuller-refusjon-ved-tilskuddsperiode")
    fun annullerRefusjon(@RequestBody annullerRefusjon: AnnullerRefusjon) {
        logger.info("Annullerer refusjon med tilskuddsperiodeId ${annullerRefusjon.tilskuddsperiodeId}")
        refusjonRepository.findAllByRefusjonsgrunnlag_Tilskuddsgrunnlag_TilskuddsperiodeId(annullerRefusjon.tilskuddsperiodeId)
            .firstOrNull()
            ?.let {
                it.annuller(true)
                it.midlerFrigjortÅrsak = MidlerFrigjortÅrsak.AVTALE_ANNULLERT
                refusjonRepository.save(it)
            }
    }

    @PostMapping("sjekk-for-klar-for-innsending")
    fun sjekkForKlarforInnsending() {
        statusJobb.settForTidligTilKlarForInnsendingHvisMulig()
    }

    @PostMapping("sjekk-for-utgaatt")
    fun sjekkForUtgaatt(@RequestParam inkluderForTidlig: Boolean = false, @RequestParam dryRun: Boolean = false): List<Refusjon> {
        val refusjoner = statusJobb.refusjonerSomKanSettesTilUtgaatt(inkluderForTidlig)
        if (!dryRun) {
            statusJobb.settTilUtgaattHvisMulig(refusjoner)
        }
        return refusjoner
    }

    @PostMapping("send-utgaatt-melding-for-refusjon")
    fun sendUtgaattMeldingForRefusjon(@RequestBody refusjon: RefusjonRequest): ResponseEntity<String> {
        val refusjon = refusjonRepository.findByIdOrNull(refusjon.refusjonId) ?: throw RessursFinnesIkkeException()
        return if (refusjon.status == RefusjonStatus.UTGÅTT) {
            refusjonKafkaProducer!!.refusjonUtgått(RefusjonUtgått(refusjon))
            ok("refusjonUtgått-melding sendt")
        } else {
            ResponseEntity.badRequest().body("Refusjon er ikke utgått!")
        }
    }

    @PostMapping("reberegn-dry/{id}")
    fun reberegnDryRun(@PathVariable id: String, @RequestBody request: ReberegnRequest): Beregning {
        val refusjon: Refusjon = refusjonRepository.findByIdOrNull(id) ?: throw RessursFinnesIkkeException()
        return beregnRefusjonsbeløp(
            inntekter = refusjon.refusjonsgrunnlag.inntektsgrunnlag!!.inntekter.toList(),
            tilskuddsgrunnlag = refusjon.refusjonsgrunnlag.tilskuddsgrunnlag,
            tidligereUtbetalt = 0,
            korrigertBruttoLønn = refusjon.refusjonsgrunnlag.endretBruttoLønn,
            fratrekkRefunderbarSum = refusjon.refusjonsgrunnlag.refunderbarBeløp,
            forrigeRefusjonMinusBeløp = request.minusBeløp,
            tilskuddFom = refusjon.refusjonsgrunnlag.tilskuddsgrunnlag.tilskuddFom,
            harFerietrekkForSammeMåned = request.harFerietrekkForSammeMåned,
            sumUtbetaltVarig = refusjon.refusjonsgrunnlag.sumUtbetaltVarig,
            ekstraFerietrekk = request.ferieTrekk,
            beregningskontekst = Beregningskontekst(grunnbelopService.alleGrunnbelop())
        )
    }

    @PostMapping("reberegn-lagre/{id}")
    @Transactional
    fun reberegn(@PathVariable id: String, @RequestBody request: ReberegnRequest): Beregning {
        val refusjon: Refusjon = refusjonRepository.findByIdOrNull(id) ?: throw RessursFinnesIkkeException()
        val beregning = beregnRefusjonsbeløp(
            inntekter = refusjon.refusjonsgrunnlag.inntektsgrunnlag!!.inntekter.toList(),
            tilskuddsgrunnlag = refusjon.refusjonsgrunnlag.tilskuddsgrunnlag,
            tidligereUtbetalt = 0,
            korrigertBruttoLønn = refusjon.refusjonsgrunnlag.endretBruttoLønn,
            fratrekkRefunderbarSum = refusjon.refusjonsgrunnlag.refunderbarBeløp,
            forrigeRefusjonMinusBeløp = request.minusBeløp,
            tilskuddFom = refusjon.refusjonsgrunnlag.tilskuddsgrunnlag.tilskuddFom,
            harFerietrekkForSammeMåned = request.harFerietrekkForSammeMåned,
            sumUtbetaltVarig = refusjon.refusjonsgrunnlag.sumUtbetaltVarig,
            ekstraFerietrekk = request.ferieTrekk,
            beregningskontekst = Beregningskontekst(grunnbelopService.alleGrunnbelop())
        )
        refusjon.refusjonsgrunnlag.forrigeRefusjonMinusBeløp = request.minusBeløp
        refusjon.refusjonsgrunnlag.beregning = beregning
        logger.info("Har oppdatert beregning på refusjon ${refusjon.id} fra admin-endepunkt")
        refusjonRepository.save(refusjon)
        return beregning
    }

    @GetMapping("hent-refusjoner-med-status-sendt")
    fun hentRefusjonerMedStatusSendtKrav() = refusjonRepository.findAllByStatus(RefusjonStatus.SENDT_KRAV)

    @PostMapping("oppdater-alle-refusjoner-klar-med-data/{page}")
    @Transactional
    fun oppdaterAlleRefusjonerKlarMedData(@PathVariable page: String) {

        val pageable = PageRequest.of(Integer.parseInt(page), 100, Sort.by(Sort.Order.asc("id")))
        val alleKlarForInnsending = refusjonRepository.findAllByStatus(RefusjonStatus.KLAR_FOR_INNSENDING, pageable)
        logger.info("Hentet alle som er klar for innsending, totalt ${alleKlarForInnsending.size} antall pages ${alleKlarForInnsending.totalPages}")
        alleKlarForInnsending.forEach {
            refusjonService.oppdaterRefusjon(it, ADMIN_BRUKER)
        }
    }

    @PostMapping("oppdater-alle-refusjoner-fortidlig-med-data/{page}")
    @Transactional
    fun oppdaterAlleRefusjonerForTidligMedData(@PathVariable page: String) {
        val pageable = PageRequest.of(Integer.parseInt(page), 200, Sort.by(Sort.Order.asc("id")))
        val alleForTidlig = refusjonRepository.findAllByStatus(RefusjonStatus.FOR_TIDLIG, pageable)
        logger.info("Hentet alle på side $page med status for tidlig, totalt ${alleForTidlig.size} antall pages ${alleForTidlig.totalPages}")
        alleForTidlig.forEach {
            refusjonService.oppdaterRefusjon(it, ADMIN_BRUKER)
        }
    }

    @PostMapping("send-refusjon-godkjent-melding")
    @Transactional
    fun sendRefusjonGodkjentMelding(@RequestBody refusjonGodkjentRequest: RefusjonGodkjentRequest): ResponseEntity<String> {
        val refusjon = refusjonRepository.findById(refusjonGodkjentRequest.refusjonId).orElseThrow()

        refusjonKafkaProducer!!.refusjonEndretStatus(RefusjonEndretStatus(refusjon))
        if (refusjon.refusjonsgrunnlag.refusjonsgrunnlagetErNullSomIZero()) {
            refusjonKafkaProducer!!.annullerTilskuddsperiodeEtterNullEllerMinusBeløp(
                refusjon,
                MidlerFrigjortÅrsak.REFUSJON_GODKJENT_NULL_BELØP
            )
            return ok("Sendt godkjent nullbeløp-melding for ${refusjon.id}")
        } else if (refusjon.refusjonsgrunnlag.refusjonsgrunnlagetErNegativt()) {
            refusjonKafkaProducer!!.annullerTilskuddsperiodeEtterNullEllerMinusBeløp(
                refusjon,
                MidlerFrigjortÅrsak.REFUSJON_MINUS_BELØP
            )
            return ok("Sendt godkjent minusbeløp-melding for ${refusjon.id}")
        } else {
            refusjonKafkaProducer!!.sendRefusjonGodkjentMelding(refusjon)
            return ok("Sendt godkjent-melding for ${refusjon.id}")
        }
    }

    @PostMapping("send-tilskuddsperiode-annullert-melding")
    @Transactional
    fun sentTilskuddsperiodeAnnullertMelding(@RequestBody annullerRefusjon: AnnullerRefusjon): ResponseEntity<String> {
        val refusjoner =
            refusjonRepository.findAllByRefusjonsgrunnlag_Tilskuddsgrunnlag_TilskuddsperiodeId(annullerRefusjon.tilskuddsperiodeId)
        if (refusjoner.size > 1) {
            return ResponseEntity.badRequest()
                .body("Fant flere refusjoner med tilskuddsperiodeId ${annullerRefusjon.tilskuddsperiodeId}")
        }
        val refusjon = refusjoner.firstOrNull() ?: return ResponseEntity.badRequest()
            .body("Fant ingen refusjon med tilskuddsperiodeId ${annullerRefusjon.tilskuddsperiodeId}")

        refusjonKafkaProducer!!.refusjonEndretStatus(RefusjonEndretStatus(refusjon))
        if (refusjon.refusjonsgrunnlag.refusjonsgrunnlagetErNullSomIZero()) {
            refusjonKafkaProducer!!.annullerTilskuddsperiodeEtterNullEllerMinusBeløp(
                refusjon,
                MidlerFrigjortÅrsak.REFUSJON_GODKJENT_NULL_BELØP
            )
            return ok("Sendt godkjent nullbeløp-melding for ${refusjon.id}")
        } else if (refusjon.refusjonsgrunnlag.refusjonsgrunnlagetErNegativt()) {
            refusjonKafkaProducer!!.annullerTilskuddsperiodeEtterNullEllerMinusBeløp(
                refusjon,
                MidlerFrigjortÅrsak.REFUSJON_MINUS_BELØP
            )
            return ok("Sendt godkjent minusbeløp-melding for ${refusjon.id}")
        } else {
            return ok("Kunne ikke annullere refusjon ${refusjon.id}")
        }
    }

    @PostMapping("utfoer-automatisk-innsending")
    fun manuellAutomatiskUtbetaling() {
        automatiskInnsendingService.utførAutomatiskInnsendingHvisMulig()
    }

    @PostMapping("rapport-om-ubetalte-refusjoner")
    @Transactional
    fun rapportOmUbetalteRefusjoner() {
        ubetaltRefusjonRapport.loggUbetalteRefusjoner()
    }
}

data class ReberegnRequest(val harFerietrekkForSammeMåned: Boolean, val minusBeløp: Int, val ferieTrekk: Int)
data class KorreksjonRequest(val refusjonIder: List<String>, val korreksjonsgrunner: Set<Korreksjonsgrunn>)
data class ForlengFristerRequest(
    val refusjonIder: List<String>,
    val nyFrist: LocalDate,
    val årsak: String,
    val tillatForlengingUtoverMaksimalFrist: Boolean
)

data class RefusjonRequest(val refusjonId: String)
data class AnnullerRefusjon(val tilskuddsperiodeId: String)
data class RefusjonGodkjentRequest(val refusjonId: String)
