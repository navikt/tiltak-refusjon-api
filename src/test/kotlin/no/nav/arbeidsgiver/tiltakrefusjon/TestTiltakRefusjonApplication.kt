package no.nav.arbeidsgiver.tiltakrefusjon

import no.nav.security.token.support.test.spring.TokenGeneratorConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Profile

@SpringBootApplication
@Profile("local")
@Import(TokenGeneratorConfiguration::class)
class TestTiltakRefusjonApplication

fun main(args: Array<String>) {
    runApplication<TestTiltakRefusjonApplication>(*args) {
        setAdditionalProfiles("local")
    }
}