package no.nav.arbeidsgiver.tiltakrefusjon.varsling

import no.nav.arbeidsgiver.tiltakrefusjon.Topics
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.Refusjon
import no.nav.arbeidsgiver.tiltakrefusjon.utils.Now
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.SendResult
import org.springframework.stereotype.Component
import org.springframework.util.concurrent.ListenableFutureCallback

@ConditionalOnProperty("tiltak-refusjon.kafka.enabled")
@Component
class RefusjonVarselProducer(
    val kafkaTemplate: KafkaTemplate<String, RefusjonVarselMelding>,
    val varslingRepository: VarslingRepository
) {

    private val log = LoggerFactory.getLogger(javaClass)

    fun sendVarsel(refusjon: Refusjon, varselType: VarselType) {
        log.info("Prosesserer $varselType melding for sending på topic ${Topics.TILTAK_VARSEL}")
        val melding = RefusjonVarselMelding(
            avtaleId = refusjon.refusjonsgrunnlag.tilskuddsgrunnlag.avtaleId,
            tilskuddsperiodeId = refusjon.refusjonsgrunnlag.tilskuddsgrunnlag.tilskuddsperiodeId,
            varselType = varselType
        )
        val meldingId = "${refusjon.id}-$varselType"
        kafkaTemplate.send(Topics.TILTAK_VARSEL, meldingId, melding)
            .addCallback(object : ListenableFutureCallback<SendResult<String?, RefusjonVarselMelding?>?> {
                override fun onFailure(ex: Throwable) {
                    log.warn(
                        "Melding med id {} kunne ikke sendes til Kafka topic {}",
                        meldingId,
                        Topics.TILTAK_VARSEL
                    )
                }

                override fun onSuccess(p0: SendResult<String?, RefusjonVarselMelding?>?) {
                    log.info(
                        "Melding med id {} sendt til Kafka topic {}",
                        meldingId,
                        Topics.TILTAK_VARSEL
                    )
                    val varsling = Varsling(refusjon.id, varselType, Now.localDateTime())
                    varslingRepository.save(varsling)
                }
            })
    }
}