package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.arbeidsgiver.tiltakrefusjon.Topics
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.RefusjonGodkjentMelding.Companion.create
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.events.GodkjentAvArbeidsgiver
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.SendResult
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionalEventListener
import org.springframework.util.concurrent.ListenableFutureCallback

@ConditionalOnProperty("tiltak-refusjon.kafka.enabled")
@Component
class RefusjonKafkaProducer(val kafkaTemplate: KafkaTemplate<String, String>, val objectMapper: ObjectMapper) {

    var log: Logger = LoggerFactory.getLogger(javaClass)

    @TransactionalEventListener
    fun refusjonGodkjent(event: GodkjentAvArbeidsgiver) {
        val melding = create(event.refusjon)
        val meldingSomString: String
        meldingSomString = try {
            objectMapper.writeValueAsString(melding)
        } catch (e: JsonProcessingException) {
            log.error("Kunne ikke lage JSON for melding med id {} til topic {}",
                event.refusjon.id,
                Topics.REFUSJON_GODKJENT)
            return
        }
        kafkaTemplate.send(Topics.REFUSJON_GODKJENT, event.refusjon.id, meldingSomString)
            .addCallback(object : ListenableFutureCallback<SendResult<String?, String?>?> {
                override fun onSuccess(result: SendResult<String?, String?>?) {
                    log.info("Melding med id {} sendt til Kafka topic {}", event.refusjon.id, Topics.REFUSJON_GODKJENT)
                }

                override fun onFailure(ex: Throwable) {
                    log.warn(
                        "Melding med id {} kunne ikke sendes til Kafka topic {}",
                        event.refusjon.id,
                        Topics.REFUSJON_GODKJENT
                    )
                }
            })
    }
}