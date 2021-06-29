package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.arbeidsgiver.tiltakrefusjon.Topics
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.SendResult
import org.springframework.util.concurrent.ListenableFutureCallback

class RefusjonVarselProducer(val kafkaTemplate: KafkaTemplate<String, RefusjonKlarMelding>, val objectMapper: ObjectMapper) {

    val log = LoggerFactory.getLogger(RefusjonVarselProducer::class.java)

    fun refusjonKlar(event: Refusjon){
        val melding = RefusjonKlarMelding(event.tilskuddsgrunnlag.avtaleId)
        kafkaTemplate.send(Topics.TILTAK_VARSEL, event.tilskuddsgrunnlag.avtaleId, melding)
            .addCallback(object : ListenableFutureCallback<SendResult<String?, RefusjonKlarMelding?>?> {

                override fun onFailure(ex: Throwable) {
                    log.warn(
                        "Melding med id {} kunne ikke sendes til Kafka topic {}",
                        event.tilskuddsgrunnlag.avtaleId,
                        Topics.REFUSJON_GODKJENT
                    )
                }

                override fun onSuccess(p0: SendResult<String?, RefusjonKlarMelding?>?) {
                    log.info("Melding med id {} sendt til Kafka topic {}", event.tilskuddsgrunnlag.avtaleId, Topics.REFUSJON_GODKJENT)
                }
            })

    }
}