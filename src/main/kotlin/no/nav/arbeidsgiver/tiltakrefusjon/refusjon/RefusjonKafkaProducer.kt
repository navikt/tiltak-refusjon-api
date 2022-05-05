package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import no.nav.arbeidsgiver.tiltakrefusjon.Topics
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.RefusjonGodkjentMelding.Companion.create
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.events.GodkjentAvArbeidsgiver
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.events.KorreksjonSendtTilUtbetaling
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.events.TilskuddsperioderIRefusjonAnnullertManuelt
import no.nav.arbeidsgiver.tiltakrefusjon.tilskuddsperiode.TilskuddsperiodeAnnullertMelding
import no.nav.arbeidsgiver.tiltakrefusjon.tilskuddsperiode.TilskuddsperiodeAnnullertÅrsak
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
    val tilskuddperiodeAnnullertKafkaTemplate: KafkaTemplate<String, TilskuddsperiodeAnnullertMelding>,
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
            korreksjonId = event.korreksjon.id,
            avtaleNr = event.korreksjon.refusjonsgrunnlag.tilskuddsgrunnlag.avtaleNr,
            løpenummer = event.korreksjon.refusjonsgrunnlag.tilskuddsgrunnlag.løpenummer,
            avtaleId = event.korreksjon.refusjonsgrunnlag.tilskuddsgrunnlag.avtaleId,
            tilskuddsperiodeId = event.korreksjon.refusjonsgrunnlag.tilskuddsgrunnlag.tilskuddsperiodeId,
            beløp = event.korreksjon.refusjonsgrunnlag.beregning!!.refusjonsbeløp,
            korreksjonsnummer = event.korreksjon.korreksjonsnummer,
            bedriftKontonummer = event.korreksjon.refusjonsgrunnlag.bedriftKontonummer!!,
            korrigererRefusjonId = event.korreksjon.korrigererRefusjonId,
            kostnadssted = event.korreksjon.kostnadssted!!
        )
        korreksjonKafkaTemplate.send(Topics.REFUSJON_KORRIGERT, event.korreksjon.id, melding)
            .addCallback({
                log.info(
                    "Melding med id {} sendt til Kafka topic {}",
                    it?.producerRecord?.key(),
                    it?.recordMetadata?.topic()
                )
            }, {
                log.warn("Feil ved sending av refusjon korrigert-melding på Kafka", it)
            })
    }

    @TransactionalEventListener
    fun refusjonAnnullertManuelt(event: TilskuddsperioderIRefusjonAnnullertManuelt) {
        // Annullering av tilskuddsperiode til tiltak-okonomi. refusjon-api vil ikke gjøre noe med denne pga årsak.
        val tilskuddperiodeAnnullertMelding = TilskuddsperiodeAnnullertMelding(
            tilskuddsperiodeId = event.refusjon.tilskuddsgrunnlag.tilskuddsperiodeId,
            årsak = TilskuddsperiodeAnnullertÅrsak.REFUSJON_IKKE_SØKT
        )
        tilskuddperiodeAnnullertKafkaTemplate.send(
            Topics.TILSKUDDSPERIODE_ANNULLERT,
            event.refusjon.tilskuddsgrunnlag.tilskuddsperiodeId,
            tilskuddperiodeAnnullertMelding
        )
            .addCallback({
                log.info(
                    "Melding med id {} sendt til Kafka topic {}",
                    it?.producerRecord?.key(),
                    it?.recordMetadata?.topic()
                )
            }, {
                log.warn("Feil ved sending av tilskuddsperiode annullert melding på Kafka", it)
            })

    }
}