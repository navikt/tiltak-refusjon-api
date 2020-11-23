package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import no.nav.arbeidsgiver.tiltakrefusjon.inntekt.InntektskomponentConsumer
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class RefusjonsberegningService(val refusjonRepository: RefusjonRepository, val inntektskomponentConsumer: InntektskomponentConsumer) {
    fun hentGrunnlag(refusjonsberegningRequest: RefusjonsberegningRequest): Refusjonsgrunnlag {
        if (!refusjonsberegningRequest.erUtfylt()) {
            throw RefusjonException("Refusjonsberegning er ikke riktig utfylt")
        }
        val refusjonFraDato = LocalDate.parse(refusjonsberegningRequest.refusjonFraDato)
        val refusjonTilDato = LocalDate.parse(refusjonsberegningRequest.refusjonTilDato)
        val inntekter = inntektskomponentConsumer.hentInntekter(refusjonsberegningRequest.fnr, refusjonsberegningRequest.bedriftNr, refusjonFraDato, refusjonTilDato)
        val refusjon: Refusjon = refusjonRepository.findByDeltakerBedriftOgPeriode(refusjonsberegningRequest.fnr, refusjonsberegningRequest.bedriftNr, refusjonFraDato, refusjonTilDato)
                ?: throw RefusjonException("Refusjon ikke funnet")
        return Refusjonsgrunnlag(inntekter, refusjon)
    }

    fun hentGrunnlag(refusjonId: String): Refusjonsgrunnlag {
        val refusjon: Refusjon = refusjonRepository.findByIdOrNull(refusjonId)
                ?: throw RefusjonException("Refusjon ikke funnet")
        val inntekter = inntektskomponentConsumer.hentInntekter(refusjon.deltakerFnr, refusjon.bedriftnummer, refusjon.fraDato, refusjon.tilDato)
        return Refusjonsgrunnlag(inntekter, refusjon)
    }
}