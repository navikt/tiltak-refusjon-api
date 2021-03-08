package no.nav.arbeidsgiver.tiltakrefusjon

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Profile


@SpringBootApplication
@Profile("dockercompose")
class DockerComposeTiltakRefusjonApplication

fun main(args: Array<String>) {
    runApplication<DockerComposeTiltakRefusjonApplication>(*args) {
        setAdditionalProfiles("dockercompose","wiremock", "testdata")
    }
}