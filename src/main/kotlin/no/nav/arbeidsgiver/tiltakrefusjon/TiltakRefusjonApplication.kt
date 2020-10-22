package no.nav.arbeidsgiver.tiltakrefusjon

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import kotlin.system.exitProcess

@SpringBootApplication
class TiltakRefusjonApplication

fun main(args: Array<String>) {
    runApplication<TiltakRefusjonApplication>(*args) {
        if (System.getenv("NAIS_CLUSTER_NAME") == null) {
            println("Kan ikke startes uten miljøvariabel NAIS_CLUSTER_NAME. Lokalt kan TestTiltakRefusjonApplication kjøres.")
            exitProcess(1)
        }
        setAdditionalProfiles(System.getenv("NAIS_CLUSTER_NAME"))
    }
}
