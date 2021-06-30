package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.arbeidsgiver.tiltakrefusjon.Topics
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.events.RefusjonKlar
import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.SendResult
import org.springframework.util.concurrent.ListenableFutureCallback

class RefusjonVarselProducer(val kafkaTemplate: KafkaTemplate<String, RefusjonKlarMelding>, val objectMapper: ObjectMapper) {

    val log = LoggerFactory.getLogger(RefusjonVarselProducer::class.java)

    @EventListener
    fun refusjonKlar(event: RefusjonKlar){
        val melding = RefusjonKlarMelding(event.refusjon.tilskuddsgrunnlag.avtaleId)
        kafkaTemplate.send(Topics.TILTAK_VARSEL, event.refusjon.tilskuddsgrunnlag.avtaleId, melding)
            .addCallback(object : ListenableFutureCallback<SendResult<String?, RefusjonKlarMelding?>?> {

                override fun onFailure(ex: Throwable) {
                    log.warn(
                        "Melding med id {} kunne ikke sendes til Kafka topic {}",
                        event.refusjon.tilskuddsgrunnlag.avtaleId,
                        Topics.REFUSJON_GODKJENT
                    )
                }

                override fun onSuccess(p0: SendResult<String?, RefusjonKlarMelding?>?) {
                    log.info("Melding med id {} sendt til Kafka topic {}", event.refusjon.tilskuddsgrunnlag.avtaleId, Topics.REFUSJON_GODKJENT)
                }
            })

    }
}