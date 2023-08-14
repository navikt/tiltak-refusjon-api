package no.nav.arbeidsgiver.tiltakrefusjon

import no.nav.arbeidsgiver.tiltakrefusjon.leader.LeaderPodCheck
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.*
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
            service.opprettKorreksjonsutkast(refusjon, korreksjonRequest.korreksjonsgrunner, 2)
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
                refusjon.forlengFrist(request.nyFrist, request.årsak, "admin", request.enforce)
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
        val refusjoner = refusjonRepository.findAllByFristForGodkjenningBeforeAndStatus(request.tilDato, RefusjonStatus.KLAR_FOR_INNSENDING)
        logger.info("Fant ${refusjoner.size} refusjoner som skal forlenges")
        var fristerForlenget = 0
        for (refusjon in refusjoner) {
            try {
                refusjon.forlengFrist(request.nyFrist, request.årsak, "admin", request.enforce)
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
    @PostMapping("annuller-tilskuddsperioder-manuelt")
    fun annullerTilskuddsperioderIRefusjonManuelt(@RequestBody request: AnnullerTilskuddsperioderRequest) {
        logger.info("Bruker AdminController for å annullere tilskuddsperioder i {} refusjoner", request.refusjonIder.size)
        for (id in request.refusjonIder) {
            val refusjon =
                refusjonRepository.findByIdOrNull(id) ?: throw RuntimeException("Finner ikke refusjon med id=$id")
            refusjon.annullerTilskuddsperioderIRefusjon(request.utførtAv, request.årsak)
            refusjonRepository.save(refusjon)
        }
    }

    @Unprotected
    @PostMapping("annuller-tilskuddsperioder-manuelt-i-utgåtte-refusjoner")
    fun annullerTilskuddsperioderIUtgåtteRefusjonManuelt(@RequestBody request: AnnullerTilskuddsperioderIUtgåtteRefusjonerRequest) {
        val utgåtteRefusjoner = refusjonRepository.findAllByStatus(RefusjonStatus.UTGÅTT)
        logger.info("Bruker AdminController for å annullere tilskuddsperioder i {} utgåtte refusjoner", utgåtteRefusjoner.size)
        utgåtteRefusjoner.forEach {
            it.annullerTilskuddsperioderIRefusjon(request.utførtAv, request.årsak)
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
    @PostMapping("reberegn-dry/{id}/{medForrigeMinus}")
    fun reberegnDryRun(@PathVariable id: String, @PathVariable medForrigeMinus: Boolean): Beregning {
        val refusjon: Refusjon = refusjonRepository.findByIdOrNull(id) ?: throw RessursFinnesIkkeException()
        return beregnRefusjonsbeløp(
            inntekter = refusjon.refusjonsgrunnlag.inntektsgrunnlag!!.inntekter.toList(),
            tilskuddsgrunnlag = refusjon.refusjonsgrunnlag.tilskuddsgrunnlag,
            tidligereUtbetalt = 0,
            korrigertBruttoLønn = refusjon.refusjonsgrunnlag.endretBruttoLønn,
            fratrekkRefunderbarSum =refusjon.refusjonsgrunnlag.refunderbarBeløp,
            forrigeRefusjonMinusBeløp =  if (medForrigeMinus) refusjon.refusjonsgrunnlag.forrigeRefusjonMinusBeløp else 0,
            tilskuddFom = refusjon.refusjonsgrunnlag.tilskuddsgrunnlag.tilskuddFom,
            harFerietrekkForSammeMåned = false,
            sumUtbetaltVarig = refusjon.refusjonsgrunnlag.sumUtbetaltVarig
        )
    }

    @Unprotected
    @PostMapping("reberegn/{id}/{medForrigeMinus}")
    @Transactional
    fun reberegn(@PathVariable id: String, @PathVariable medForrigeMinus: Boolean): Beregning {
        val refusjon: Refusjon = refusjonRepository.findByIdOrNull(id) ?: throw RessursFinnesIkkeException()
        val beregning =  beregnRefusjonsbeløp(
            inntekter = refusjon.refusjonsgrunnlag.inntektsgrunnlag!!.inntekter.toList(),
            tilskuddsgrunnlag = refusjon.refusjonsgrunnlag.tilskuddsgrunnlag,
            tidligereUtbetalt = 0,
            korrigertBruttoLønn = refusjon.refusjonsgrunnlag.endretBruttoLønn,
            fratrekkRefunderbarSum = refusjon.refusjonsgrunnlag.refunderbarBeløp,
            forrigeRefusjonMinusBeløp =  if (medForrigeMinus) refusjon.refusjonsgrunnlag.forrigeRefusjonMinusBeløp else 0,
            tilskuddFom = refusjon.refusjonsgrunnlag.tilskuddsgrunnlag.tilskuddFom,
            harFerietrekkForSammeMåned = false,
            sumUtbetaltVarig = refusjon.refusjonsgrunnlag.sumUtbetaltVarig
        )
        refusjon.refusjonsgrunnlag.beregning = beregning
        refusjonRepository.save(refusjon)
        return beregning
    }

}

data class KorreksjonRequest(val refusjonIder: List<String>, val korreksjonsgrunner: Set<Korreksjonsgrunn>)

data class ForlengFristerRequest(val refusjonIder: List<String>, val nyFrist: LocalDate, val årsak: String, val enforce: Boolean)
data class ForlengFristerTilOgMedRequest(val tilDato: LocalDate, val nyFrist: LocalDate, val årsak: String, val enforce: Boolean)

data class AnnullerTilskuddsperioderRequest(val refusjonIder: List<String>, val utførtAv: String, val årsak: String)
data class AnnullerTilskuddsperioderIUtgåtteRefusjonerRequest(val utførtAv: String, val årsak: String)