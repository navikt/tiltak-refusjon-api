package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.events.GodkjentAvArbeidsgiver
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Component
class RefusjonGodkjentLytter(
    val refusjonRepository: RefusjonRepository,
    val minusbelopRepository: MinusbelopRepository
) {


    @TransactionalEventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun refusjonGodkjent(event: GodkjentAvArbeidsgiver) {
        // Her kan man nullstille eventuelle minusbeløp
        val refusjon = refusjonRepository.findByIdOrNull(event.refusjon.id) ?: throw RuntimeException("Finner ikke refusjon med id=${event.refusjon.id}")
        val alleMinusBeløpPåAvtalen = minusbelopRepository.findAllByAvtaleNr(refusjon.refusjonsgrunnlag.tilskuddsgrunnlag.avtaleNr);
        // Er det minusbeløp på avtalen kan de nå nullstilles da refusjonen er godkjent uten minusbeløp
        println("Skal vi nullstille?")
        alleMinusBeløpPåAvtalen.forEach{
            it.beløp = 0
            //minusbelopRepository.delete(it)
            minusbelopRepository.save(it)
            println("ja..")
        }
    }
}