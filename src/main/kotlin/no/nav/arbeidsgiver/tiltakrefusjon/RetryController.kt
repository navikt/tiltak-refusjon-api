package no.nav.arbeidsgiver.tiltakrefusjon

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.RefusjonService
import no.nav.arbeidsgiver.tiltakrefusjon.tilskuddsperiode.TilskuddsperiodeGodkjentMelding
import no.nav.security.token.support.core.api.Unprotected
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class RetryController(val service: RefusjonService, val objectMapper: ObjectMapper) {
    var logger = LoggerFactory.getLogger(javaClass)

    @Unprotected
    @GetMapping
    fun hjem():String{
        return "Hei fra Refusjon-api controller"
    }

    @Unprotected
    @PostMapping("/retry/refusjon/opprett/")
    fun opprettRefusjon(@RequestBody jsonMelding:String): String {
        logger.info("Bruker ADMIN RETRY Controller for opprett refusjon: {}", jsonMelding)
        service.opprettRefusjon(objectMapper.readValue(jsonMelding, TilskuddsperiodeGodkjentMelding::class.java))
        return "Refusjon opprettet"
    }
}