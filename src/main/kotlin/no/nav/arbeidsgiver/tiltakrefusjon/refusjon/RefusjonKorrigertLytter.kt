package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.events.KorreksjonMerketForOppgjort
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.events.KorreksjonMerketForTilbakekreving
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.events.KorreksjonSendtTilUtbetaling
import no.nav.arbeidsgiver.tiltakrefusjon.varsling.RefusjonVarselProducer
import no.nav.arbeidsgiver.tiltakrefusjon.varsling.VarselType
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionalEventListener

@Component
@ConditionalOnProperty("tiltak-refusjon.kafka.enabled")
class RefusjonKorrigertLytter(
    val producer: RefusjonVarselProducer
) {
    @TransactionalEventListener
    fun korreksjonSendtTilUtbetaling(event: KorreksjonSendtTilUtbetaling) {
        sendVarsel(event.korreksjon)
    }

    @TransactionalEventListener
    fun korreksjonMerketForTilbakekreving(event: KorreksjonMerketForTilbakekreving) {
        sendVarsel(event.korreksjon)
    }

    @TransactionalEventListener
    fun korreksjonMerketForOppgjort(event: KorreksjonMerketForOppgjort) {
        sendVarsel(event.korreksjon)
    }

    private fun sendVarsel(korreksjon: Korreksjon) {
        producer.sendVarsel(
            varselType = VarselType.KORRIGERT,
            refusjonId = korreksjon.korrigererRefusjonId,
            tilskuddsperiodeId = korreksjon.refusjonsgrunnlag.tilskuddsgrunnlag.tilskuddsperiodeId,
            avtaleId = korreksjon.refusjonsgrunnlag.tilskuddsgrunnlag.avtaleId
        )
    }
}