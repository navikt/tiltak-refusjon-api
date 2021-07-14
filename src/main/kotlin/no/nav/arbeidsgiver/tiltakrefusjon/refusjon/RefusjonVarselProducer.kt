package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.arbeidsgiver.tiltakrefusjon.Topics
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.SendResult
import org.springframework.stereotype.Component
import org.springframework.util.concurrent.ListenableFutureCallback
import java.time.LocalDateTime

@ConditionalOnProperty("tiltak-refusjon.kafka.enabled")
@Component
class RefusjonVarselProducer(
    val kafkaTemplate: KafkaTemplate<String, RefusjonVarselMelding>,
    val varslingRepository: VarslingRepository
) {

    val log = LoggerFactory.getLogger(RefusjonVarselProducer::class.java)

    fun sendVarsel(refusjon: Refusjon, varselType: VarselType) {
        log.info("prosesserer $varselType melding for sending på topic ${Topics.TILTAK_VARSEL}")
        val melding = RefusjonVarselMelding(refusjon.tilskuddsgrunnlag.avtaleId, varselType)
        kafkaTemplate.send(Topics.TILTAK_VARSEL, "${refusjon.id}-$varselType", melding)
            .addCallback(object : ListenableFutureCallback<SendResult<String?, RefusjonVarselMelding?>?> {
                override fun onFailure(ex: Throwable) {
                    log.warn(
                        "Melding med id {} kunne ikke sendes til Kafka topic {}",
                        refusjon.tilskuddsgrunnlag.avtaleId,
                        Topics.TILTAK_VARSEL
                    )
                }

                override fun onSuccess(p0: SendResult<String?, RefusjonVarselMelding?>?) {
                    log.info(
                        "Melding med id {} sendt til Kafka topic {}",
                        refusjon.tilskuddsgrunnlag.avtaleId,
                        Topics.TILTAK_VARSEL
                    )
                    val varsling = Varsling(refusjon.id, varselType, LocalDateTime.now())
                    varslingRepository.save(varsling)
                }
            })
    }
}