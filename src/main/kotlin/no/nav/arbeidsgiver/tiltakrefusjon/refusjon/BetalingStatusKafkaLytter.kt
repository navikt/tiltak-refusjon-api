package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.arbeidsgiver.tiltakrefusjon.Topics.REFUSJON_ENDRET_BETALINGSSTATUS
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Profile
import org.springframework.data.repository.findByIdOrNull
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty("tiltak-refusjon.kafka.enabled")
@Profile("local","dev-gcp")
class BetalingStatusKafkaLytter(private val refusjonRepository: RefusjonRepository, private val objectMapper: ObjectMapper) {

    val log: Logger = LoggerFactory.getLogger(javaClass)

    @KafkaListener(topics = [REFUSJON_ENDRET_BETALINGSSTATUS])
    fun oppdaterRefusjonStatusBasertPåBetalingStatusFraØkonomi(event: String) {
        val betalingStatus = objectMapper.readValue(event,BetalingStatusEndringMelding::class.java)
        val refusjon = refusjonRepository.findByIdOrNull(betalingStatus.refusjonId)
        if(refusjon == null){
            log.error("Mottatt en betaling status for en ukjent refusjon  ${betalingStatus.refusjonId}")
            return
        }
        if(betalingStatus.erBetalt()) {
            refusjon.utbetalingVellykket()
        } else {
            refusjon.utbetalingMislykket()
        }

        refusjonRepository.save(refusjon)
    }
}