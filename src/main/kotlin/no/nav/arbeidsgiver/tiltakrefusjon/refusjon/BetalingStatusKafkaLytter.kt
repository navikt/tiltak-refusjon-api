package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.arbeidsgiver.tiltakrefusjon.Topics.REFUSJON_ENDRET_BETALINGSSTATUS
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.data.repository.findByIdOrNull
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty("tiltak-refusjon.kafka.enabled")
class BetalingStatusKafkaLytter(private val refusjonRepository: RefusjonRepository, private val objectMapper: ObjectMapper) {

    val log: Logger = LoggerFactory.getLogger(javaClass)

    @KafkaListener(topics = [REFUSJON_ENDRET_BETALINGSSTATUS])
    fun oppdaterRefusjonStatusBasertPåBetalingStatusFraØkonomi(event: String) {
        val betalingsstatus = objectMapper.readValue(event,BetalingStatusEndringMelding::class.java)
        val refusjon = refusjonRepository.findByIdOrNull(betalingsstatus.refusjonId)
        if(refusjon == null){
            log.error("Mottatt en betaling status for en ukjent refusjon  ${betalingsstatus.refusjonId}")
            return
        }
        if(betalingsstatus.erUtbetalt()) {
            refusjon.utbetalingVellykket()
        } else {
            refusjon.utbetalingMislykket()
        }

        refusjonRepository.save(refusjon)
    }
}