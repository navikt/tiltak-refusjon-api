package no.nav.arbeidsgiver.tiltakrefusjon.tilskudd

import com.fasterxml.jackson.databind.ObjectMapper
import lombok.RequiredArgsConstructor
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.RefusjonService
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@ConditionalOnProperty("tiltak-refusjon.kafka.enabled")
@Component
@RequiredArgsConstructor
class GodkjentTilskuddLytter(val service: RefusjonService, val objectMapper: ObjectMapper) {

    @KafkaListener(topics = [Topics.REFUSJON])
    fun consume(tilskuddMelding: String) {
        val tilskuddMelding = objectMapper.readValue(tilskuddMelding, TilskuddMelding::class.java)
        service.opprettRefusjon(tilskuddMelding)
    }
}