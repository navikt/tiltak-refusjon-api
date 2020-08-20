package no.nav.arbeidsgiver.tiltakrefusjon

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Profile

@SpringBootApplication
@Profile("local")
class TestTiltakRefusjonApplication

fun main(args: Array<String>) {
    runApplication<TestTiltakRefusjonApplication>(*args) {
        setAdditionalProfiles("local")
    }
}