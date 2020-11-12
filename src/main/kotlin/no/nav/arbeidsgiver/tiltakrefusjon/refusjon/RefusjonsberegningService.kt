package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import no.nav.arbeidsgiver.tiltakrefusjon.inntekt.InntektskomponentConsumer
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class RefusjonsberegningService(val refusjonRepository: RefusjonRepository, val inntektskomponentConsumer: InntektskomponentConsumer) {
    fun hentGrunnlag(refusjonsberegningRequest: RefusjonsberegningRequest):Refusjonsgrunnlag{
        if(!refusjonsberegningRequest.erUtfylt()){
            throw RefusjonException("Refusjonsberegning er ikke riktig utfylt")
        }
        val refusjonFraDato = LocalDate.parse(refusjonsberegningRequest.refusjonFraDato)
        val refusjonTilDato = LocalDate.parse(refusjonsberegningRequest.refusjonTilDato)
        val inntekter = inntektskomponentConsumer.hentInntekter(refusjonsberegningRequest.fnr,refusjonsberegningRequest.bedriftNr,refusjonFraDato,refusjonTilDato)
        val refusjon: Refusjon = refusjonRepository.findOneByDeltakerFnrAndBedriftnummerAndFraDatoGreaterThanEqualAndTilDatoLessThanEqual(refusjonsberegningRequest.fnr, refusjonsberegningRequest.bedriftNr, refusjonFraDato, refusjonTilDato)
                ?: throw RefusjonException("Refusjon ikke funnet")
        return Refusjonsgrunnlag(inntekter,refusjon)
    }

}