package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import no.nav.arbeidsgiver.tiltakrefusjon.inntekt.InntektskomponentConsumer
import no.nav.arbeidsgiver.tiltakrefusjon.tilskudd.TilskuddMelding
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class RefusjonService(
        val inntektskomponentConsumer: InntektskomponentConsumer,
        val refusjonRepository: RefusjonRepository
) {
    fun opprettRefusjon(tilskuddMelding: TilskuddMelding): Refusjon {
        val tilskuddsgrunnlag = Tilskuddsgrunnlag(
                avtaleId = tilskuddMelding.avtaleId,
                tilskuddsperiodeId = tilskuddMelding.tilskuddsperiodeId,
                deltakerFornavn = tilskuddMelding.deltakerFornavn,
                deltakerEtternavn = tilskuddMelding.deltakerEtternavn,
                deltakerFnr = tilskuddMelding.deltakerFnr,
                veilederNavIdent = tilskuddMelding.veilederNavIdent,
                bedriftNavn = tilskuddMelding.bedriftNavn,
                bedriftNr = tilskuddMelding.bedriftNr,
                tilskuddFom = tilskuddMelding.tilskuddFom,
                tilskuddTom = tilskuddMelding.tilskuddTom,
                feriepengerSats = tilskuddMelding.feriepengerSats,
                otpSats = tilskuddMelding.otpSats,
                arbeidsgiveravgiftSats = tilskuddMelding.arbeidsgiveravgiftSats,
                tiltakstype = tilskuddMelding.tiltakstype,
                tilskuddsbeløp = tilskuddMelding.tilskuddsbeløp,
                lønnstilskuddsprosent = tilskuddMelding.lønnstilskuddsprosent
        )
        val refusjon = Refusjon(tilskuddsgrunnlag = tilskuddsgrunnlag, deltakerFnr = tilskuddMelding.deltakerFnr, bedriftNr = tilskuddMelding.bedriftNr)
        return refusjonRepository.save(refusjon)
    }

    fun hentInntekterForRefusjon(refusjon: Refusjon) {
        val inntektsgrunnlag = Inntektsgrunnlag(
                inntekter = inntektskomponentConsumer.hentInntekter(
                        refusjon.deltakerFnr,
                        refusjon.bedriftNr,
                        refusjon.tilskuddsgrunnlag.tilskuddFom,
                        refusjon.tilskuddsgrunnlag.tilskuddTom
                ))

        refusjon.inntektsgrunnlag = inntektsgrunnlag
        refusjon.refusjonsbeløp = beregnRefusjonsbeløp(inntektsgrunnlag.inntekter, refusjon.tilskuddsgrunnlag)

        refusjonRepository.save(refusjon)
    }
}