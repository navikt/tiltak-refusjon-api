package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.arbeidsgiver.tiltakrefusjon.Topics
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.events.RefusjonKlar
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.SendResult
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionalEventListener
import org.springframework.util.concurrent.ListenableFutureCallback

@ConditionalOnProperty("tiltak-refusjon.kafka.enabled")
@Component
class RefusjonVarselProducer(val kafkaTemplate: KafkaTemplate<String, RefusjonVarselMelding>, val objectMapper: ObjectMapper) {

    val log = LoggerFactory.getLogger(RefusjonVarselProducer::class.java)

    @TransactionalEventListener
    fun refusjonKlar(event: RefusjonKlar){
        log.info("refusjon-klar melding prosesseres for sending på topic: ${Topics.TILTAK_VARSEL} med key: ${event.refusjon.id}")
        val melding = RefusjonVarselMelding(event.refusjon.tilskuddsgrunnlag.avtaleId, VarselType.KLAR)
        kafkaTemplate.send(Topics.TILTAK_VARSEL, "${event.refusjon.id}-KLAR", melding)
            .addCallback(object : ListenableFutureCallback<SendResult<String?, RefusjonVarselMelding?>?> {

                override fun onFailure(ex: Throwable) {
                    log.warn(
                        "Melding med id {} kunne ikke sendes til Kafka topic {}",
                        event.refusjon.tilskuddsgrunnlag.avtaleId,
                        Topics.TILTAK_VARSEL
                    )
                }

                override fun onSuccess(p0: SendResult<String?, RefusjonVarselMelding?>?) {
                    log.info("Melding med id {} sendt til Kafka topic {}", event.refusjon.tilskuddsgrunnlag.avtaleId, Topics.TILTAK_VARSEL)
                }
            })

    }
}