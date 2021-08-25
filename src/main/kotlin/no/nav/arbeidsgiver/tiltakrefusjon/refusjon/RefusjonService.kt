package no.nav.arbeidsgiver.tiltakrefusjon.refusjon


import no.nav.arbeidsgiver.tiltakrefusjon.inntekt.InntektskomponentService
import no.nav.arbeidsgiver.tiltakrefusjon.okonomi.KontoregisterService
import no.nav.arbeidsgiver.tiltakrefusjon.tilskuddsperiode.TilskuddsperiodeAnnullertMelding
import no.nav.arbeidsgiver.tiltakrefusjon.tilskuddsperiode.TilskuddsperiodeForkortetMelding
import no.nav.arbeidsgiver.tiltakrefusjon.tilskuddsperiode.TilskuddsperiodeGodkjentMelding
import no.nav.arbeidsgiver.tiltakrefusjon.utils.Now
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class RefusjonService(
    val inntektskomponentService: InntektskomponentService,
    val refusjonRepository: RefusjonRepository,
    val kontoregisterService: KontoregisterService,
    @Value("\${NAIS_APP_IMAGE:}")
    val appImageId: String,
) {
    val log = LoggerFactory.getLogger(javaClass)

    fun opprettRefusjon(tilskuddsperiodeGodkjentMelding: TilskuddsperiodeGodkjentMelding) {
        log.info("Oppretter refusjon for tilskuddsperiodeId ${tilskuddsperiodeGodkjentMelding.tilskuddsperiodeId}")

        if (refusjonRepository.findByTilskuddsgrunnlag_TilskuddsperiodeId(tilskuddsperiodeGodkjentMelding.tilskuddsperiodeId) != null) {
            log.warn("Refusjon finnes allerede for tilskuddsperiode med id ${tilskuddsperiodeGodkjentMelding.tilskuddsperiodeId}")
            return;
        }

        val tilskuddsgrunnlag = Tilskuddsgrunnlag(
            avtaleId = tilskuddsperiodeGodkjentMelding.avtaleId,
            tilskuddsperiodeId = tilskuddsperiodeGodkjentMelding.tilskuddsperiodeId,
            deltakerFornavn = tilskuddsperiodeGodkjentMelding.deltakerFornavn,
            deltakerEtternavn = tilskuddsperiodeGodkjentMelding.deltakerEtternavn,
            deltakerFnr = tilskuddsperiodeGodkjentMelding.deltakerFnr,
            veilederNavIdent = tilskuddsperiodeGodkjentMelding.veilederNavIdent,
            bedriftNavn = tilskuddsperiodeGodkjentMelding.bedriftNavn,
            bedriftNr = tilskuddsperiodeGodkjentMelding.bedriftNr,
            tilskuddFom = tilskuddsperiodeGodkjentMelding.tilskuddFom,
            tilskuddTom = tilskuddsperiodeGodkjentMelding.tilskuddTom,
            feriepengerSats = tilskuddsperiodeGodkjentMelding.feriepengerSats,
            otpSats = tilskuddsperiodeGodkjentMelding.otpSats,
            arbeidsgiveravgiftSats = tilskuddsperiodeGodkjentMelding.arbeidsgiveravgiftSats,
            tiltakstype = tilskuddsperiodeGodkjentMelding.tiltakstype,
            tilskuddsbeløp = tilskuddsperiodeGodkjentMelding.tilskuddsbeløp,
            lønnstilskuddsprosent = tilskuddsperiodeGodkjentMelding.lønnstilskuddsprosent,
            avtaleNr = tilskuddsperiodeGodkjentMelding.avtaleNr,
            løpenummer = tilskuddsperiodeGodkjentMelding.løpenummer,
            enhet = tilskuddsperiodeGodkjentMelding.enhet,
        )
        val refusjon = Refusjon(
            tilskuddsgrunnlag = tilskuddsgrunnlag,
            deltakerFnr = tilskuddsperiodeGodkjentMelding.deltakerFnr,
            bedriftNr = tilskuddsperiodeGodkjentMelding.bedriftNr,
        )

        refusjonRepository.save(refusjon)
    }

    fun gjørInntektsoppslag(refusjon: Refusjon) {
        if (refusjon.inntektsgrunnlag != null && refusjon.inntektsgrunnlag!!.innhentetTidspunkt.plusMinutes(1)
                .isAfter(Now.localDateTime())
        ) {
            return
        }

        val inntektsoppslag = inntektskomponentService.hentInntekter(
            fnr = refusjon.deltakerFnr,
            bedriftnummerDetSøkesPå = refusjon.bedriftNr,
            datoFra = refusjon.tilskuddsgrunnlag.tilskuddFom,
            datoTil = if (refusjon.korreksjonsgrunner.contains(Korreksjonsgrunn.INNTEKTER_RAPPORTERT_UTENFOR_OPPTJENINGSPERIODE))
                refusjon.tilskuddsgrunnlag.tilskuddTom.plusMonths(1)
            else
                refusjon.tilskuddsgrunnlag.tilskuddTom
        )
        val inntektsgrunnlag = Inntektsgrunnlag(
            inntekter = inntektsoppslag.first,
            respons = inntektsoppslag.second
        )

        val tidligereUtbetalt = beregnTidligereUtbetaltBeløp(refusjon)

        refusjon.oppgiInntektsgrunnlag(inntektsgrunnlag,
            appImageId,
            tidligereUtbetalt)

        refusjonRepository.save(refusjon)
    }

    private fun beregnTidligereUtbetaltBeløp(refusjon: Refusjon) =
        if (refusjon.korreksjonAvId !== null) {
            if (refusjon.korreksjonsgrunner.contains(Korreksjonsgrunn.UTBETALT_HELE_TILSKUDDSBELØP)) {
                refusjon.tilskuddsgrunnlag.tilskuddsbeløp
            } else {
                val opprinneligRefusjon = refusjonRepository.findByIdOrNull(refusjon.korreksjonAvId)!!
                opprinneligRefusjon.beregning!!.refusjonsbeløp
            }
        } else {
            0
        }

    fun godkjennForArbeidsgiver(refusjon: Refusjon) {
        refusjon.godkjennForArbeidsgiver()
        refusjonRepository.save(refusjon)
    }

    fun annullerRefusjon(melding: TilskuddsperiodeAnnullertMelding) {
        log.info("Annullerer refusjon med tilskuddsperiodeId ${melding.tilskuddsperiodeId}")
        val refusjon = refusjonRepository.findByTilskuddsgrunnlag_TilskuddsperiodeId(melding.tilskuddsperiodeId)
        if (refusjon != null) {
            refusjon.annuller()
            refusjonRepository.save(refusjon)
        } else {
            log.warn("Kunne ikke annullere refusjon med tilskuddsperiodeId ${melding.tilskuddsperiodeId}, fant ikke refusjonen")
        }
    }

    fun forkortRefusjon(melding: TilskuddsperiodeForkortetMelding) {
        log.info("Forkorter refusjon med tilskuddsperiodeId ${melding.tilskuddsperiodeId} til dato ${melding.tilskuddTom} med nytt beløp ${melding.tilskuddsbeløp}")
        val refusjon = refusjonRepository.findByTilskuddsgrunnlag_TilskuddsperiodeId(melding.tilskuddsperiodeId)
        if (refusjon != null) {
            refusjon.forkort(melding.tilskuddTom, melding.tilskuddsbeløp)
            refusjonRepository.save(refusjon)
        } else {
            log.warn("Kunne ikke forkorte refusjon med tilskuddsperiodeId ${melding.tilskuddsperiodeId}, fant ikke refusjonen")
        }
    }

    fun gjørBedriftKontonummeroppslag(refusjon: Refusjon) {
        if (refusjon.innhentetBedriftKontonummerTidspunkt != null && refusjon.innhentetBedriftKontonummerTidspunkt!!.isAfter(
                Now.localDateTime().minusMinutes(1))
        ) {
            return
        }
        refusjon.oppgiBedriftKontonummer(kontoregisterService.hentBankkontonummer(refusjon.bedriftNr))

        refusjonRepository.save(refusjon)
    }
}