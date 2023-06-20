package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.events.RefusjonGodkjentMinusBeløp
import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
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
    val log = LoggerFactory.getLogger(javaClass)

    @EventListener
    fun refusjonGodkjentMinusbeløp(event: RefusjonGodkjentMinusBeløp) {
        // Lagre et minusbeløp. Må evt dra fra gamle minusbeløp så det ikke blir dobbelt opp
        val refusjon = refusjonRepository.findByIdOrNull(event.refusjon.id) ?: throw RuntimeException("Finner ikke refusjon med id=${event.refusjon.id}")

        // Nullstill alle gamle beløp. Det er nå gjort opp og vi lagrer nytt minusbeløp på denne refusjonen.
        val alleMinusBeløp = minusbelopRepository.findAllByAvtaleNr(refusjon.tilskuddsgrunnlag.avtaleNr)
        alleMinusBeløp.forEach {
            it.gjortOpp = true
            minusbelopRepository.save(it)
        }
        val minusbelop = Minusbelop (
            avtaleNr = refusjon.refusjonsgrunnlag.tilskuddsgrunnlag.avtaleNr,
            beløp = refusjon.refusjonsgrunnlag.beregning?.refusjonsbeløp,
            løpenummer = refusjon.refusjonsgrunnlag.tilskuddsgrunnlag.løpenummer)

        refusjon.minusbelop = minusbelop
        log.info("Setter minusbeløp ${minusbelop.id} på refusjon ${refusjon.id}")
        refusjonRepository.save(refusjon)
        minusbelopRepository.save(minusbelop)
    }
}
