package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import no.nav.arbeidsgiver.tiltakrefusjon.inntekt.InntektskomponentConsumer
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class RefusjonsberegningService(val refusjonRepository: RefusjonRepository, val inntektskomponentConsumer: InntektskomponentConsumer) {

    fun hentGrunnlag(refusjonsberegningRequest: RefusjonsberegningRequest):Refusjonsgrunnlag{
        val refusjon:Refusjon =  refusjonRepository.findOneByDeltakerFnrAndBedriftnummerAndFraDatoGreaterThanEqualAndTilDatoLessThanEqual(refusjonsberegningRequest.fnr!!, refusjonsberegningRequest.bedriftNr!!, LocalDate.parse(refusjonsberegningRequest.refusjonFraDato!!), LocalDate.parse(refusjonsberegningRequest.refusjonTilDato!!))
        val inntekter = inntektskomponentConsumer.hentInntekter(refusjonsberegningRequest)
        return Refusjonsgrunnlag(inntekter,refusjon.stillingsprosent,refusjon.fraDato,refusjon.tilDato,refusjon.satsArbeidsgiveravgift,refusjon.satsFeriepenger,0.02)
    }

}