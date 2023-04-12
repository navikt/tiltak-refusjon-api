package no.nav.arbeidsgiver.tiltakrefusjon.featuretoggles

import no.finn.unleash.strategy.Strategy
import org.springframework.stereotype.Component
import java.util.*

@Component
class ByEnvironmentStrategy : Strategy {
    val environment: String
    override fun getName(): String {
        return "byEnvironment"
    }

    override fun isEnabled(parameters: Map<String, String>): Boolean {
        return Optional.ofNullable(parameters)
            .map { map: Map<String, String> -> map["miljø"] }
            .map { env: String? -> Arrays.asList(*env!!.split(",").toTypedArray()).contains(environment) }
            .orElse(false)
    }

    init {
        environment = Optional.ofNullable(System.getenv("MILJO")).orElse("local")
    }
}