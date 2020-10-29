package no.nav.arbeidsgiver.tiltakrefusjon

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Profile


@SpringBootApplication
@Profile("local")
class LokalTiltakRefusjonApplication

fun main(args: Array<String>) {
    runApplication<LokalTiltakRefusjonApplication>(*args) {
        setAdditionalProfiles("local","wiremock")
    }
}