package no.nav.arbeidsgiver.tiltakrefusjon.tilskudd

import lombok.RequiredArgsConstructor
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.RefusjonService
import no.nav.security.token.support.core.api.Protected
import org.springframework.context.annotation.Profile
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@Profile("local")
@RestController
@RequiredArgsConstructor
@Protected
class GodkjentTilskuddTestController(val beregningService: RefusjonService) {

    @PostMapping
    fun consume(@RequestBody melding: TilskuddMelding) {
        beregningService.opprettRefusjon(melding)
    }
}