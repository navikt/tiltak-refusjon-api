package no.nav.arbeidsgiver.tiltakrefusjon.tilskuddsperiode

import lombok.RequiredArgsConstructor
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.RefusjonService
import no.nav.security.token.support.core.api.Protected
import no.nav.security.token.support.core.api.Unprotected
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Profile
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@ConditionalOnProperty("tiltak-refusjon.kafka.fake")
@RestController
@RequiredArgsConstructor
@Unprotected
@RequestMapping("/fake-kafka")
class TilskuddsperiodeFakeKafkaController(val beregningService: RefusjonService) {

    @PostMapping("tilskuddsperiode-godkjent")
    fun tilskuddsperiodeGodkjent(@RequestBody melding: TilskuddsperiodeGodkjentMelding) {
        beregningService.opprettRefusjon(melding)
    }
}