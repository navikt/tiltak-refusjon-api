package no.nav.arbeidsgiver.tiltakrefusjon

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.retry.annotation.EnableRetry
import org.springframework.scheduling.annotation.EnableScheduling
import kotlin.system.exitProcess

@SpringBootApplication
@EnableScheduling
@EnableRetry
class TiltakRefusjonApplication

fun main(args: Array<String>) {
    runApplication<TiltakRefusjonApplication>(*args) {
        if (System.getenv("MILJO") == null) {
            println("Kan ikke startes uten miljøvariabel MILJO. Lokalt kan LokalTiltakRefusjonApplication kjøres.")
            exitProcess(1)
        }
        setAdditionalProfiles(System.getenv("MILJO"))
    }
}
