package no.nav.arbeidsgiver.tiltakrefusjon.tilskuddsperiode

import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.RefusjonService
import no.nav.security.token.support.core.api.Unprotected
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@ConditionalOnProperty("tiltak-refusjon.kafka.fake")
@RestController
@Unprotected
@RequestMapping("/fake-kafka")
class TilskuddsperiodeFakeKafkaController(val refusjonService: RefusjonService) {

    @PostMapping("tilskuddsperiode-godkjent")
    fun tilskuddsperiodeGodkjent(@RequestBody melding: TilskuddsperiodeGodkjentMelding) {
        refusjonService.opprettRefusjon(melding)
    }

    @PostMapping("tilskuddsperiode-annullert")
    fun tilskuddsperiodeAnnullert(@RequestBody melding: TilskuddsperiodeAnnullertMelding) {
        refusjonService.annullerRefusjon(melding)
    }

    @PostMapping("tilskuddsperiode-forkortet")
    fun tilskuddsperiodeForkortet(@RequestBody melding: TilskuddsperiodeForkortetMelding) {
        refusjonService.forkortRefusjon(melding)
    }
}