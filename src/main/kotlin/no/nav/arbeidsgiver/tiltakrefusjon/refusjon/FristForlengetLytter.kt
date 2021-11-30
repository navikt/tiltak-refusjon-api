package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.events.FristForlenget
import no.nav.arbeidsgiver.tiltakrefusjon.varsling.RefusjonVarselProducer
import no.nav.arbeidsgiver.tiltakrefusjon.varsling.VarselType
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty("tiltak-refusjon.kafka.enabled")
class FristForlengetLytter(
    val fristForlengetRepository: FristForlengetRepository,
    val producer: RefusjonVarselProducer,
) {
    @EventListener
    fun fristForlenget(event: FristForlenget) {
        fristForlengetRepository.save(
            FristForlengetEntitet(
                refusjonId = event.refusjon.id,
                gammelFrist = event.gammelFrist,
                nyFrist = event.nyFrist,
                årsak = event.årsak,
                utførtAv = event.utførtAv
            )
        )
        producer.sendVarsel(
            varselType = VarselType.FRIST_FORLENGET,
            refusjonId = event.refusjon.id,
            tilskuddsperiodeId = event.refusjon.refusjonsgrunnlag.tilskuddsgrunnlag.tilskuddsperiodeId,
            avtaleId = event.refusjon.refusjonsgrunnlag.tilskuddsgrunnlag.avtaleId
        )
    }
}