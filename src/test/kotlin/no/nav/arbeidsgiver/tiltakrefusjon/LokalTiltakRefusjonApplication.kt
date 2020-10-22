package no.nav.arbeidsgiver.tiltakrefusjon

import no.nav.security.token.support.test.spring.TokenGeneratorConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Profile

@SpringBootApplication
@Profile("local","wiremock")
@Import(TokenGeneratorConfiguration::class)
class LokalTiltakRefusjonApplication

fun main(args: Array<String>) {
    runApplication<LokalTiltakRefusjonApplication>(*args) {
        setAdditionalProfiles("local","wiremock")
    }
}