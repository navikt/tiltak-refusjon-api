package no.nav.arbeidsgiver.tiltakrefusjon

import no.nav.arbeidsgiver.tiltakrefusjon.autorisering.ADMIN_BRUKER
import no.nav.arbeidsgiver.tiltakrefusjon.leader.LeaderPodCheck
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.*
import no.nav.arbeidsgiver.tiltakrefusjon.tilskuddsperiode.MidlerFrigjortÅrsak
import no.nav.arbeidsgiver.tiltakrefusjon.tilskuddsperiode.TilskuddsperiodeForkortetMelding
import no.nav.arbeidsgiver.tiltakrefusjon.tilskuddsperiode.TilskuddsperiodeGodkjentMelding
import no.nav.security.token.support.core.api.Unprotected
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

@RestController
@RequestMapping("/internal/admin")
class AdminController(
    val service: RefusjonService,
    val refusjonRepository: RefusjonRepository,
    val refusjonService: RefusjonService,
    val leaderPodCheck: LeaderPodCheck
) {
    val logger = LoggerFactory.getLogger(javaClass)

    @Unprotected
    @PostMapping("opprett-refusjon")
    fun opprettRefusjon(@RequestBody jsonMelding: TilskuddsperiodeGodkjentMelding): Refusjon? {
        logger.info(
            "Bruker AdminController for å opprette refusjon med tilskuddsperiodeId {}",
            jsonMelding.tilskuddsperiodeId
        )
        return service.opprettRefusjon(jsonMelding)
    }

    @Unprotected
    @PostMapping("opprett-refusjoner")
    fun opprettRefusjoner(@RequestBody jsonMeldinger: List<TilskuddsperiodeGodkjentMelding>) {
        jsonMeldinger.forEach {
            logger.info(
                "Bruker AdminController for å opprette refusjon med tilskuddsperiodeId {}",
                it.tilskuddsperiodeId
            )
            service.opprettRefusjon(it)
        }
    }

    @Unprotected
    @PostMapping("forkort-tilskuddsperiode")
    fun forkortTilskuddsperiode(@RequestBody jsonMelding: TilskuddsperiodeForkortetMelding) {
        logger.info(
            "Bruker AdminController for å forkorte tilskuddsperiode med tilskuddsperiodeId {}",
            jsonMelding.tilskuddsperiodeId
        )
        service.forkortRefusjon(jsonMelding)
    }

    @Unprotected
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

    // Kanskje ikke behov. Ble brukt ved migrering til ny datamodell
    // @Unprotected
    // @PostMapping("slett-korreksjoner")
    // fun slettKorreksjoner(@RequestBody refusjonIder: List<String>) {
    //     logger.info("Bruker AdminController for å slette korreksjon på {} refusjoner", refusjonIder.size)
    //     for (id in refusjonIder) {
    //         val refusjon =
    //             refusjonRepository.findByIdOrNull(id) ?: throw RuntimeException("Finner ikke refusjon med id=$id")
    //         service.slettKorreksjonsutkast(refusjon)
    //     }
    // }

    @Unprotected
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
                refusjon.forlengFrist(request.nyFrist, request.årsak, ADMIN_BRUKER, request.enforce)
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

    @Unprotected
    @PostMapping("forleng-frister-til-og-med-dato")
    fun forlengFristerTilOgMedDato(@RequestBody request: ForlengFristerTilOgMedRequest) {
        logger.info("Bruker AdminController for å forlenge refusjoner med frist før ${request.tilDato} til ny frist: ${request.nyFrist}")
        val refusjoner = refusjonRepository.findAllByFristForGodkjenningBeforeAndStatus(
            request.tilDato,
            RefusjonStatus.KLAR_FOR_INNSENDING
        )
        logger.info("Fant ${refusjoner.size} refusjoner som skal forlenges")
        var fristerForlenget = 0

        for (refusjon in refusjoner) {
            try {
                refusjon.forlengFrist(request.nyFrist, request.årsak, ADMIN_BRUKER, request.enforce)
                refusjonRepository.save(refusjon)
                fristerForlenget++
            } catch (e: FeilkodeException) {
                if (e.feilkode == Feilkode.FOR_LANG_FORLENGELSE_AV_FRIST) {
                    logger.warn("Forlengelse av frist på refusjon med id=${refusjon.id} overskrider grensen på 1 måned")
                } else {
                    logger.error("Feil ved forlengelse av frist på refusjon med id=${refusjon.id}", e.stackTrace)
                    throw e
                }
            }
        }
        logger.info("Forlenget frister på $fristerForlenget refusjoner")
    }

    @Unprotected
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

    @Unprotected
    @PostMapping("sjekk-for-klar-for-innsending")
    fun sjekkForKlarforInnsending() {
        StatusJobb(refusjonRepository, leaderPodCheck).sjekkOmKlarForInnsending()
    }

    @Unprotected
    @PostMapping("sjekk-for-utgått")
    fun sjekkForUtgått() {
        StatusJobb(refusjonRepository, leaderPodCheck).sjekkOmUtgått()
    }

    @Unprotected
    @PostMapping("reberegn-dry/{id}")
    fun reberegnDryRun(@PathVariable id: String, @RequestBody request: ReberegnRequest): Beregning {
        val refusjon: Refusjon = refusjonRepository.findByIdOrNull(id) ?: throw RessursFinnesIkkeException()
        return beregnRefusjonsbeløp(
            inntekter = refusjon.refusjonsgrunnlag.inntektsgrunnlag!!.inntekter.toList(),
            tilskuddsgrunnlag = refusjon.refusjonsgrunnlag.tilskuddsgrunnlag,
            tidligereUtbetalt = 0,
            korrigertBruttoLønn = refusjon.refusjonsgrunnlag.endretBruttoLønn,
            fratrekkRefunderbarSum =refusjon.refusjonsgrunnlag.refunderbarBeløp,
            forrigeRefusjonMinusBeløp = request.minusBeløp,
            tilskuddFom = refusjon.refusjonsgrunnlag.tilskuddsgrunnlag.tilskuddFom,
            harFerietrekkForSammeMåned = request.harFerietrekkForSammeMåned,
            sumUtbetaltVarig = refusjon.refusjonsgrunnlag.sumUtbetaltVarig,
            ekstraFerietrekk = request.ferieTrekk
        )
    }

    @Unprotected
    @PostMapping("reberegn-lagre/{id}")
    @Transactional
    fun reberegn(@PathVariable id: String, @RequestBody request: ReberegnRequest): Beregning {
        val refusjon: Refusjon = refusjonRepository.findByIdOrNull(id) ?: throw RessursFinnesIkkeException()
        val beregning =  beregnRefusjonsbeløp(
            inntekter = refusjon.refusjonsgrunnlag.inntektsgrunnlag!!.inntekter.toList(),
            tilskuddsgrunnlag = refusjon.refusjonsgrunnlag.tilskuddsgrunnlag,
            tidligereUtbetalt = 0,
            korrigertBruttoLønn = refusjon.refusjonsgrunnlag.endretBruttoLønn,
            fratrekkRefunderbarSum = refusjon.refusjonsgrunnlag.refunderbarBeløp,
            forrigeRefusjonMinusBeløp = request.minusBeløp,
            tilskuddFom = refusjon.refusjonsgrunnlag.tilskuddsgrunnlag.tilskuddFom,
            harFerietrekkForSammeMåned = request.harFerietrekkForSammeMåned,
            sumUtbetaltVarig = refusjon.refusjonsgrunnlag.sumUtbetaltVarig,
            ekstraFerietrekk = request.ferieTrekk
        )
        refusjon.refusjonsgrunnlag.forrigeRefusjonMinusBeløp = request.minusBeløp
        refusjon.refusjonsgrunnlag.beregning = beregning
        logger.info("Har oppdatert beregning på refusjon ${refusjon.id} fra admin-endepunkt")
        refusjonRepository.save(refusjon)
        return beregning
    }

    @Unprotected
    @GetMapping("hent-refusjoner-med-status-sendt")
    fun hentRefusjonerMedStatusSendtKrav()  = refusjonRepository.findAllByStatus(RefusjonStatus.SENDT_KRAV)

    @Unprotected
    @PostMapping("oppdater-alle-refusjoner-med-data")
    @Transactional
    fun oppdaterAlleRefusjonerMedData() {
        val alleKlarForInnsending = refusjonRepository.findAllByStatus(RefusjonStatus.KLAR_FOR_INNSENDING);
        logger.info("Hentet alle som er klar for innsending, totalt ${alleKlarForInnsending.size}")
        alleKlarForInnsending.forEach {
            refusjonService.oppdaterRefusjon(it, ADMIN_BRUKER)
        }
        val alleForTidlig = refusjonRepository.findAllByStatus(RefusjonStatus.FOR_TIDLIG);
        logger.info("Hentet alle med status for tidlig, totalt ${alleForTidlig.size}")
        alleForTidlig.forEach {
            refusjonService.oppdaterRefusjon(it, ADMIN_BRUKER)
        }
    }
}

data class ReberegnRequest(val harFerietrekkForSammeMåned: Boolean, val minusBeløp: Int, val ferieTrekk: Int)
data class KorreksjonRequest(val refusjonIder: List<String>, val korreksjonsgrunner: Set<Korreksjonsgrunn>)
data class ForlengFristerRequest(val refusjonIder: List<String>, val nyFrist: LocalDate, val årsak: String, val enforce: Boolean)
data class ForlengFristerTilOgMedRequest(val tilDato: LocalDate, val nyFrist: LocalDate, val årsak: String, val enforce: Boolean)
data class AnnullerRefusjon(val tilskuddsperiodeId: String)
