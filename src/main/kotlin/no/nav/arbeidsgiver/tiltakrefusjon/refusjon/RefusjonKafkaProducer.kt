package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import no.nav.arbeidsgiver.tiltakrefusjon.Topics
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.RefusjonGodkjentMelding.Companion.create
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.events.*
import no.nav.arbeidsgiver.tiltakrefusjon.tilskuddsperiode.TilskuddsperiodeAnnullertMelding
import no.nav.arbeidsgiver.tiltakrefusjon.tilskuddsperiode.MidlerFrigjortÅrsak
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
    val refusjonEndretStatusKafkaTemplate: KafkaTemplate<String, RefusjonEndretStatusMelding>
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
            årsak = MidlerFrigjortÅrsak.REFUSJON_IKKE_SØKT
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

    @TransactionalEventListener
    fun refusjonUtgått(event: RefusjonUtgått) {
        // Annullering av tilskuddsperiode til tiltak-okonomi. refusjon-api vil ikke gjøre noe med denne pga årsak.
        val tilskuddperiodeAnnullertMelding = TilskuddsperiodeAnnullertMelding(
            tilskuddsperiodeId = event.refusjon.tilskuddsgrunnlag.tilskuddsperiodeId,
            årsak = MidlerFrigjortÅrsak.REFUSJON_FRIST_UTGÅTT
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

    @TransactionalEventListener
    fun refusjonGodkjentMinusBeløp(event: RefusjonGodkjentMinusBeløp) {
        log.info("Godkjent refusjon ${event.refusjon.id} med minusbeløp, sender annullering")
        annullerTilskuddsperiodeEtterNullEllerMinusBeløp(event.refusjon.tilskuddsgrunnlag.tilskuddsperiodeId, MidlerFrigjortÅrsak.REFUSJON_MINUS_BELØP)
    }

    @TransactionalEventListener
    fun refusjonGodkjentNullBeløp(event: RefusjonGodkjentNullBeløp) {
        log.info("Godkjent refusjon ${event.refusjon.id} med nullbeløp, sender annullering")
        annullerTilskuddsperiodeEtterNullEllerMinusBeløp(event.refusjon.tilskuddsgrunnlag.tilskuddsperiodeId, MidlerFrigjortÅrsak.REFUSJON_GODKJENT_NULL_BELØP)
    }

    private fun annullerTilskuddsperiodeEtterNullEllerMinusBeløp(tilskuddsperiodeId: String, årsak: MidlerFrigjortÅrsak) {
        val tilskuddperiodeAnnullertMelding = TilskuddsperiodeAnnullertMelding(
            tilskuddsperiodeId = tilskuddsperiodeId,
            årsak = årsak
        )
        tilskuddperiodeAnnullertKafkaTemplate.send(
            Topics.TILSKUDDSPERIODE_ANNULLERT,
            tilskuddsperiodeId,
            tilskuddperiodeAnnullertMelding
        ).addCallback({
            log.info(
                "Melding med id {} sendt til Kafka topic {}",
                it?.producerRecord?.key(),
                it?.recordMetadata?.topic()
            )
        }, {
            log.warn("Feil ved sending av tilskuddsperiode annullert melding på Kafka", it)
        })
    }

    // En topic med alle statuser for en refusjon. Da kan den aggregeres av fager for å vise det de vil
    @TransactionalEventListener
    fun refusjonEndretStatus(event: RefusjonEndretStatus) {
        val melding = RefusjonEndretStatusMelding(
            refusjonId = event.refusjon.id,
            bedriftNr = event.refusjon.bedriftNr,
            avtaleId = event.refusjon.refusjonsgrunnlag.tilskuddsgrunnlag.avtaleId,
            status = event.refusjon.status,
            tilskuddsperiodeId = event.refusjon.refusjonsgrunnlag.tilskuddsgrunnlag.tilskuddsperiodeId
        )
        refusjonEndretStatusKafkaTemplate.send(
            Topics.REFUSJON_ENDRET_STATUS,
            event.refusjon.id,
            melding
        ).addCallback({
            log.info("Melding med id {} sendt til Kafka topic {}", it?.producerRecord?.key(), it?.recordMetadata?.topic())
        }, {
            log.warn("Feil ved sending av refusjon status på Kafka", it)
        })
    }

}
