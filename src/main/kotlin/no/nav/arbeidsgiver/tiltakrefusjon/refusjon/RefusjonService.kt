package no.nav.arbeidsgiver.tiltakrefusjon.refusjon


import no.nav.arbeidsgiver.tiltakrefusjon.inntekt.InntektskomponentService
import no.nav.arbeidsgiver.tiltakrefusjon.okonomi.KontoregisterkomponentService
import no.nav.arbeidsgiver.tiltakrefusjon.tilskuddsperiode.TilskuddsperiodeAnnullertMelding
import no.nav.arbeidsgiver.tiltakrefusjon.tilskuddsperiode.TilskuddsperiodeForkortetMelding
import no.nav.arbeidsgiver.tiltakrefusjon.tilskuddsperiode.TilskuddsperiodeGodkjentMelding
import no.nav.arbeidsgiver.tiltakrefusjon.utils.Now
import org.springframework.stereotype.Service

@Service
class RefusjonService(
    val inntektskomponentService: InntektskomponentService,
    val refusjonRepository: RefusjonRepository,
    val kontoregisterkomponentService: KontoregisterkomponentService,
) {
    fun opprettRefusjon(tilskuddsperiodeGodkjentMelding: TilskuddsperiodeGodkjentMelding): Refusjon {
        val bedriftKontonummer = kontoregisterkomponentService.hentBankkontonummer(tilskuddsperiodeGodkjentMelding.bedriftNr)
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
            bedriftKontonummer = bedriftKontonummer
        )
        val refusjon = Refusjon(
            tilskuddsgrunnlag = tilskuddsgrunnlag,
            deltakerFnr = tilskuddsperiodeGodkjentMelding.deltakerFnr,
            bedriftNr = tilskuddsperiodeGodkjentMelding.bedriftNr,
        )
        return refusjonRepository.save(refusjon)
    }

    fun gjørInntektsoppslag(refusjon: Refusjon) {
        if (refusjon.inntektsgrunnlag != null && refusjon.inntektsgrunnlag!!.innhentetTidspunkt.isAfter(Now.localDateTime().minusMinutes(1))) {
            return
        }
        val inntektsgrunnlag = Inntektsgrunnlag(
            inntekter = inntektskomponentService.hentInntekter(
                refusjon.deltakerFnr,
                refusjon.bedriftNr,
                refusjon.tilskuddsgrunnlag.tilskuddFom,
                refusjon.tilskuddsgrunnlag.tilskuddTom
            )
        )

        refusjon.oppgiInntektsgrunnlag(inntektsgrunnlag)

        refusjonRepository.save(refusjon)
    }

    fun godkjennForArbeidsgiver(refusjon: Refusjon) {
        refusjon.godkjennForArbeidsgiver()
        refusjonRepository.save(refusjon)
    }

    fun annullerRefusjon(melding: TilskuddsperiodeAnnullertMelding) {
        val refusjon = refusjonRepository.findByTilskuddsgrunnlag_TilskuddsperiodeId(melding.tilskuddsperiodeId)
        if (refusjon != null) {
            refusjon.annuller()
            refusjonRepository.save(refusjon)
        }
    }

    fun forkortRefusjon(melding: TilskuddsperiodeForkortetMelding) {
        val refusjon = refusjonRepository.findByTilskuddsgrunnlag_TilskuddsperiodeId(melding.tilskuddsperiodeId)
        if (refusjon != null) {
            refusjon.forkort(melding.tilskuddTom, melding.tilskuddsbeløp)
            refusjonRepository.save(refusjon)
        }
    }
}