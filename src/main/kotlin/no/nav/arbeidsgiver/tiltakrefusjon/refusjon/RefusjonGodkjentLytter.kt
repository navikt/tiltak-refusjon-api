package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.events.GodkjentAvArbeidsgiver
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.event.TransactionalEventListener

@Component
class RefusjonGodkjentLytter(
    val refusjonRepository: RefusjonRepository,
    val minusbelopRepository: MinusbelopRepository,
    val refusjonService: RefusjonService
) {
    /**
     * B00m! TransactionalEventListener kjøres efter at transaksjonen som sender eventet er committed. Altså når den lagret til databasen.
     * Altså når refusjonen er godkjent
     *
     * Vi starter da en ny transaksjon som kan kjøre diverse oppdateringen på minusbeløp og andre ting.
     *
     * Dette blir da ikke med i originaltransaksjonen. Er det dumt?
     */
    @TransactionalEventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun refusjonGodkjent(event: GodkjentAvArbeidsgiver) {
        /*
        // Her kan man nullstille eventuelle minusbeløp
        val refusjon = refusjonRepository.findByIdOrNull(event.refusjon.id) ?: throw RuntimeException("Finner ikke refusjon med id=${event.refusjon.id}")
        val alleMinusBeløpPåAvtalen = minusbelopRepository.findAllByAvtaleNr(refusjon.refusjonsgrunnlag.tilskuddsgrunnlag.avtaleNr);
        // Er det minusbeløp på avtalen kan de nå nullstilles da refusjonen er godkjent uten minusbeløp
        println("Hoho, nå gjør vi opp minusbeløpen :D :D :D")
        alleMinusBeløpPåAvtalen.forEach {
            if (!it.gjortOpp) {
                it.gjortOpp = true
                it.gjortOppAvRefusjonId = refusjon.id
                minusbelopRepository.save(it)
            }
        }

        // Oppdater ikke innsendte refusjoner med data (f eks maksbløp, ferietrekk etc..)
        // Hvordan unngå at man finner denne refusjonen i spørringen? hmm.. Dette er litt rart rent transaksjonsmessig
        val alleRefusjonserSomSkalSendesInn =
            refusjonRepository.findAllByRefusjonsgrunnlag_Tilskuddsgrunnlag_AvtaleNrAndStatusIn(
                refusjon.refusjonsgrunnlag.tilskuddsgrunnlag.avtaleNr,
                listOf(RefusjonStatus.FOR_TIDLIG, RefusjonStatus.KLAR_FOR_INNSENDING)
            )
        alleRefusjonserSomSkalSendesInn.forEach {
            if(it.id != refusjon.id) {
                println("Oppdaterer nå refusjon ${it.id} da den skal sendes in ${it.status}")
                refusjonService.oppdaterRefusjon(it)
                refusjonRepository.save(it)
            }
        }
*/
    }
}