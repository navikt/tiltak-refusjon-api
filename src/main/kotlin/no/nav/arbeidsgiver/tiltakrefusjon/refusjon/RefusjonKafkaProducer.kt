package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import no.nav.arbeidsgiver.tiltakrefusjon.Topics
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.RefusjonGodkjentMelding.Companion.create
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.events.GodkjentAvArbeidsgiver
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.events.KorreksjonSendtTilUtbetaling
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
class RefusjonKafkaProducer(
    val refusjonGodkjentkafkaTemplate: KafkaTemplate<String, RefusjonGodkjentMelding>,
    val korreksjonKafkaTemplate: KafkaTemplate<String, KorreksjonSendtTilUtbetalingMelding>,
) {

    var log: Logger = LoggerFactory.getLogger(javaClass)

    @TransactionalEventListener
    fun refusjonGodkjent(event: GodkjentAvArbeidsgiver) {
        val melding = create(event.refusjon)
        refusjonGodkjentkafkaTemplate.send(Topics.REFUSJON_GODKJENT, event.refusjon.id, melding)
            .addCallback(object : ListenableFutureCallback<SendResult<String?, RefusjonGodkjentMelding?>?> {
                override fun onSuccess(result: SendResult<String?, RefusjonGodkjentMelding?>?) {
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

    @TransactionalEventListener
    fun korreksjonSendtTilUtbetaling(event: KorreksjonSendtTilUtbetaling) {
        val melding = KorreksjonSendtTilUtbetalingMelding(
            refusjonId = event.refusjon.id,
            korreksjonAvRefusjonId = event.refusjon.korreksjonAvId!!,
            avtaleId = event.refusjon.tilskuddsgrunnlag.avtaleId,
            tilskuddsperiodeId = event.refusjon.tilskuddsgrunnlag.tilskuddsperiodeId,
            beløp = event.refusjon.beregning!!.refusjonsbeløp,
            korreksjonsnummer = event.refusjon.korreksjonsnummer!!,
            bedriftKontonummer = event.refusjon.bedriftKontonummer!!,
            korreksjonstype = event.korreksjonstype
        )
        korreksjonKafkaTemplate.send(Topics.REFUSJON_KORRIGERT, event.refusjon.id, melding)
            .addCallback({
                log.info("Melding med id {} sendt til Kafka topic {}",
                    it?.producerRecord?.key(),
                    it?.recordMetadata?.topic())
            }, {
                log.warn("Feil", it)
            })
    }
}