package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.arbeidsgiver.tiltakrefusjon.Topics
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import java.time.LocalDate

@ConditionalOnProperty("tiltak-refusjon.kafka.enabled")
@Component
class RefusjonGodkjentKafkaLytter(val service: RefusjonService, val objectMapper: ObjectMapper) {

    @KafkaListener(topics = [Topics.REFUSJON_GODKJENT])
    fun tilskuddsperiodeGodkjent(refusjonGodkjentMelding:  String) {
        val godkjentMelding = objectMapper.readValue(refusjonGodkjentMelding, RefusjonGodkjentMelding::class.java)
        service.godkjennForArbeidsgiver(Refusjon(tilskuddsgrunnlag = Tilskuddsgrunnlag("avtaleid","tspid",
            "Donald","duck","212","z123","Duck co","123",
             LocalDate.now(), LocalDate.now().plusMonths(2),0.12,0.2,0.12,
            Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD,123,401,1,1)
            ,"102","123"))
    }
}