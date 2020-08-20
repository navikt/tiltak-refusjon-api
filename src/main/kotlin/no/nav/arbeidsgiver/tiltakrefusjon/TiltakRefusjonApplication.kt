package no.nav.arbeidsgiver.tiltakrefusjon

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class TiltakRefusjonApplication

fun main(args: Array<String>) {
    runApplication<TiltakRefusjonApplication>(*args) {
        setAdditionalProfiles(System.getenv("NAIS_CLUSTER_NAME"))
    }
}
