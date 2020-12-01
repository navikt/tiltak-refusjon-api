package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import no.nav.arbeidsgiver.tiltakrefusjon.inntekt.InntektskomponentConsumer
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.nydatamodell.*
import no.nav.arbeidsgiver.tiltakrefusjon.tilskudd.TilskuddMelding
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class RefusjonsberegningService(
        val refusjonRepository: RefusjonRepository,
        val inntektskomponentConsumer: InntektskomponentConsumer,
        val refusjonsakRepository: RefusjonsakRepository
) {
    fun hentGrunnlag(refusjonsberegningRequest: RefusjonsberegningRequest): Refusjonsgrunnlag {
        if (!refusjonsberegningRequest.erUtfylt()) {
            throw RefusjonException("Refusjonsberegning er ikke riktig utfylt")
        }
        val refusjonFraDato = LocalDate.parse(refusjonsberegningRequest.refusjonFraDato)
        val refusjonTilDato = LocalDate.parse(refusjonsberegningRequest.refusjonTilDato)
        val inntekter = inntektskomponentConsumer.hentInntekter(refusjonsberegningRequest.fnr, refusjonsberegningRequest.bedriftNr, refusjonFraDato, refusjonTilDato)
        val refusjon: Refusjon = refusjonRepository.findOneByDeltakerFnrAndBedriftnummerAndFraDatoGreaterThanEqualAndTilDatoLessThanEqual(refusjonsberegningRequest.fnr, refusjonsberegningRequest.bedriftNr, refusjonFraDato, refusjonTilDato)
                ?: throw RefusjonException("Refusjon ikke funnet")
        return Refusjonsgrunnlag(inntekter, refusjon)
    }

    fun hentGrunnlag(refusjonId: String): Refusjonsgrunnlag {
        val refusjon: Refusjon = refusjonRepository.findByIdOrNull(refusjonId)
                ?: throw RefusjonException("Refusjon ikke funnet")
        val inntekter = inntektskomponentConsumer.hentInntekter(refusjon.deltakerFnr, refusjon.bedriftnummer, refusjon.fraDato, refusjon.tilDato)
        return Refusjonsgrunnlag(inntekter, refusjon)
    }

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
                tilskuddsbeløp = tilskuddMelding.tilskuddsbeløp
        )
        val refusjonsak = Refusjonsak(tilskuddsgrunnlag = tilskuddsgrunnlag, deltakerFnr = tilskuddMelding.deltakerFnr, bedriftNr = tilskuddMelding.bedriftNr)
        refusjonsakRepository.save(refusjonsak)
        return refusjonsak.id
    }

    fun hentInntekterForRefusjon(refusjonsakId: String) {
        val refusjon = refusjonsakRepository.findByIdOrNull(refusjonsakId) ?: throw RuntimeException()

        val inntektsgrunnlag = Inntektsgrunnlag()
        val inntekter = inntektskomponentConsumer.hentInntekter(
                refusjon.deltakerFnr,
                refusjon.bedriftNr,
                refusjon.tilskuddsgrunnlag.tilskuddFom,
                refusjon.tilskuddsgrunnlag.tilskuddTom
        ).map {
            InntektslinjeEntity(
                    inntektsgrunnlag = inntektsgrunnlag,
                    inntektType = it.inntektType,
                    måned = it.måned,
                    beløp = it.beløp,
                    opptjeningsperiodeFom = it.opptjeningsperiodeFom,
                    opptjeningsperiodeTom = it.opptjeningsperiodeTom
            )
        }
        inntektsgrunnlag.inntekter.addAll(inntekter)

        refusjon.inntektsgrunnlag = inntektsgrunnlag

        refusjonsakRepository.save(refusjon)
    }
}