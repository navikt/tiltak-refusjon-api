package no.nav.arbeidsgiver.tiltakrefusjon.refusjon


import no.nav.arbeidsgiver.tiltakrefusjon.inntekt.InntektskomponentService
import no.nav.arbeidsgiver.tiltakrefusjon.okonomi.KontoregisterService
import no.nav.arbeidsgiver.tiltakrefusjon.tilskuddsperiode.TilskuddsperiodeAnnullertMelding
import no.nav.arbeidsgiver.tiltakrefusjon.tilskuddsperiode.TilskuddsperiodeForkortetMelding
import no.nav.arbeidsgiver.tiltakrefusjon.tilskuddsperiode.TilskuddsperiodeGodkjentMelding
import no.nav.arbeidsgiver.tiltakrefusjon.utils.Now
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class RefusjonService(
        val inntektskomponentService: InntektskomponentService,
        val refusjonRepository: RefusjonRepository,
        val kontoregisterService: KontoregisterService,
        @Value("\${NAIS_APP_IMAGE:}")
        val appImageId: String
) {
    val log = LoggerFactory.getLogger(javaClass)

    fun opprettRefusjon(tilskuddsperiodeGodkjentMelding: TilskuddsperiodeGodkjentMelding): Refusjon {
        log.info("Oppretter refusjon for tilskuddsperiodeId ${tilskuddsperiodeGodkjentMelding.tilskuddsperiodeId}")

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
        return refusjonRepository.save(refusjon)
    }

    fun gjørInntektsoppslag(refusjon: Refusjon) {
        if (refusjon.inntektsgrunnlag != null && refusjon.inntektsgrunnlag!!.innhentetTidspunkt.plusMinutes(1).isAfter(Now.localDateTime())) {
            return
        }
        val inntektsoppslag = inntektskomponentService.hentInntekter(
            refusjon.deltakerFnr,
            refusjon.bedriftNr,
            refusjon.tilskuddsgrunnlag.tilskuddFom,
            refusjon.tilskuddsgrunnlag.tilskuddTom
        )
        val inntektsgrunnlag = Inntektsgrunnlag(
                inntekter = inntektsoppslag.first,
                respons = inntektsoppslag.second
        )

        refusjon.oppgiInntektsgrunnlag(inntektsgrunnlag, appImageId)

        refusjonRepository.save(refusjon)
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
        if (refusjon.innhentetBedriftKontonummerTidspunkt != null && refusjon.innhentetBedriftKontonummerTidspunkt!!.isAfter(Now.localDateTime().minusMinutes(1))) {
            return
        }
        refusjon.oppgiBedriftKontonummer(kontoregisterService.hentBankkontonummer(refusjon.bedriftNr))

        refusjonRepository.save(refusjon)
    }
}