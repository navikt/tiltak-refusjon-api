package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import no.nav.arbeidsgiver.tiltakrefusjon.inntekt.InntektskomponentService
import no.nav.arbeidsgiver.tiltakrefusjon.tilskuddsperiode.TilskuddsperiodeGodkjentMelding
import org.springframework.stereotype.Service

@Service
class RefusjonService(
    val inntektskomponentService: InntektskomponentService,
    val refusjonRepository: RefusjonRepository
) {
    fun opprettRefusjon(tilskuddsperiodeGodkjentMelding: TilskuddsperiodeGodkjentMelding): Refusjon {
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
                lønnstilskuddsprosent = tilskuddsperiodeGodkjentMelding.lønnstilskuddsprosent
        )
        val refusjon = Refusjon(tilskuddsgrunnlag = tilskuddsgrunnlag, deltakerFnr = tilskuddsperiodeGodkjentMelding.deltakerFnr, bedriftNr = tilskuddsperiodeGodkjentMelding.bedriftNr)
        return refusjonRepository.save(refusjon)
    }

    fun gjørInntektsoppslag(refusjon: Refusjon) {
        val inntektsgrunnlag = Inntektsgrunnlag(
                inntekter = inntektskomponentService.hentInntekter(
                        refusjon.deltakerFnr,
                        refusjon.bedriftNr,
                        refusjon.tilskuddsgrunnlag.tilskuddFom,
                        refusjon.tilskuddsgrunnlag.tilskuddTom
                ))

        refusjon.oppgiInntektsgrunnlag(inntektsgrunnlag)

        refusjonRepository.save(refusjon)
    }

    fun godkjennForArbeidsgiver(refusjon: Refusjon) {
        refusjon.godkjennForArbeidsgiver()
        refusjonRepository.save(refusjon)
    }
}