package no.nav.arbeidsgiver.tiltakrefusjon.varsling

import no.nav.arbeidsgiver.tiltakrefusjon.Topics
import no.nav.arbeidsgiver.tiltakrefusjon.utils.Now
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import java.time.LocalDate

@ConditionalOnProperty("tiltak-refusjon.kafka.enabled")
@Component
class RefusjonVarselProducer(
    val kafkaTemplate: KafkaTemplate<String, RefusjonVarselMelding>,
    val varslingRepository: VarslingRepository
) {

    private val log = LoggerFactory.getLogger(javaClass)

    fun sendVarsel(
        varselType: VarselType,
        refusjonId: String,
        tilskuddsperiodeId: String,
        avtaleId: String,
        fristForGodkjenning: LocalDate?
    ) {
        log.info("Prosesserer $varselType melding for sending på topic ${Topics.TILTAK_VARSEL}")
        val melding = RefusjonVarselMelding(
            avtaleId = avtaleId,
            tilskuddsperiodeId = tilskuddsperiodeId,
            varselType = varselType,
            fristForGodkjenning = fristForGodkjenning

        )
        val meldingId = "${refusjonId}-$varselType"
        kafkaTemplate.send(Topics.TILTAK_VARSEL, meldingId, melding)
            .whenComplete { it, ex ->
                if (ex != null) {
                    log.warn(
                        "Melding med id {} kunne ikke sendes til Kafka topic {}",
                        meldingId,
                        Topics.TILTAK_VARSEL
                    )
                } else {
                    log.info(
                        "Melding med id {} sendt til Kafka topic {}",
                        meldingId,
                        Topics.TILTAK_VARSEL
                    )
                    val varsling = Varsling(refusjonId, varselType, Now.localDateTime())
                    varslingRepository.save(varsling)
                }

            }
    }
}