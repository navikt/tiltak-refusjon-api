package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.arbeidsgiver.tiltakrefusjon.Topics
import no.nav.arbeidsgiver.tiltakrefusjon.enRefusjon
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@ConditionalOnProperty("tiltak-refusjon.kafka.enabled")
@Component
class RefusjonGodkjentKafkaLytter(val service: RefusjonService, val objectMapper: ObjectMapper) {

    @KafkaListener(topics = [Topics.REFUSJON_GODKJENT])
    fun tilskuddsperiodeGodkjent(refusjonGodkjentMelding:  String) {
        val godkjentMelding = objectMapper.readValue(refusjonGodkjentMelding, RefusjonGodkjentMelding::class.java)
       service.godkjennForArbeidsgiver(enRefusjon())
    }
}