package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.events.RefusjonGodkjentMinusBeløp
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.event.TransactionalEventListener

@Component
class RefusjonGodkjentMinusBeløpLytter(
    val refusjonRepository: RefusjonRepository,
    val minusbelopRepository: MinusbelopRepository
) {
    @TransactionalEventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun refusjonGodkjentMinusbeløp(event: RefusjonGodkjentMinusBeløp) {
        // Lagre et minusbeløp. Må evt dra fra gamle minusbeløp så det ikke blir dobbelt opp
        val refusjon = refusjonRepository.findByIdOrNull(event.refusjon.id) ?: throw RuntimeException("Finner ikke refusjon med id=${event.refusjon.id}")
        val alleMinusBeløpPåAvtalen = minusbelopRepository.findAllByAvtaleNr(refusjon.refusjonsgrunnlag.tilskuddsgrunnlag.avtaleNr);

        val sumMinusbelop = alleMinusBeløpPåAvtalen
            .map { minusbelop -> minusbelop.beløp}
            .filterNotNull()
            .reduceOrNull{sum, beløp -> sum + beløp}
        println("<<<<<<<<<<< total gammel sum ${sumMinusbelop}")
        val minusbelop = Minusbelop(
            avtaleNr = refusjon.refusjonsgrunnlag.tilskuddsgrunnlag.avtaleNr,
            beløp = refusjon.refusjonsgrunnlag.beregning?.refusjonsbeløp,
            løpenummer = refusjon.refusjonsgrunnlag.tilskuddsgrunnlag.løpenummer)
        if(sumMinusbelop != null) {
            minusbelop.beløp = minusbelop.beløp?.minus(sumMinusbelop)
        }
        println("<<<<<<<<<<<< refusjonens minus ${refusjon.refusjonsgrunnlag.beregning?.refusjonsbeløp}")
        println("<<<<<<<<<<<< totalt ${minusbelop.beløp}")
        refusjon.minusbelop = minusbelop
        refusjonRepository.save(refusjon)
    }
}