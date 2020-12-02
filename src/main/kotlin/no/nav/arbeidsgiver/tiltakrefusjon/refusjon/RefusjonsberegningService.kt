package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import no.nav.arbeidsgiver.tiltakrefusjon.inntekt.InntektskomponentConsumer
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.nydatamodell.*
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.nydatamodell.Inntektslinje
import no.nav.arbeidsgiver.tiltakrefusjon.tilskudd.TilskuddMelding
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class RefusjonsberegningService(
        val inntektskomponentConsumer: InntektskomponentConsumer,
        val refusjonsakRepository: RefusjonsakRepository
) {
    fun opprettRefusjon(tilskuddMelding: TilskuddMelding): String {
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
        val refusjonsak = Refusjonsak(tilskuddsgrunnlag = tilskuddsgrunnlag, deltakerFnr = tilskuddMelding.deltakerFnr, bedriftNr = tilskuddMelding.bedriftNr)
        refusjonsakRepository.save(refusjonsak)
        return refusjonsak.id
    }

    fun hentInntekterForRefusjon(refusjonsakId: String) {
        val refusjon = refusjonsakRepository.findByIdOrNull(refusjonsakId) ?: throw RuntimeException()

        val inntektsgrunnlag = Inntektsgrunnlag(
                inntekter = inntektskomponentConsumer.hentInntekter(
                        refusjon.deltakerFnr,
                        refusjon.bedriftNr,
                        refusjon.tilskuddsgrunnlag.tilskuddFom,
                        refusjon.tilskuddsgrunnlag.tilskuddTom
                ))

        refusjon.inntektsgrunnlag = inntektsgrunnlag

        refusjonsakRepository.save(refusjon)
    }
}