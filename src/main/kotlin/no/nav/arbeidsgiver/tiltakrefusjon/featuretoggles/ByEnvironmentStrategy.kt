package no.nav.arbeidsgiver.tiltakrefusjon.featuretoggles


import io.getunleash.UnleashContext
import io.getunleash.strategy.Strategy
import org.springframework.stereotype.Component
import java.util.*

@Component
class ByEnvironmentStrategy : Strategy {
    final val environment: String = Optional.ofNullable(System.getenv("MILJO")).orElse("local")

    override fun getName(): String {
        return "byEnvironment"
    }

    override fun isEnabled(
        p0: Map<String, String>,
        p1: UnleashContext
    ): Boolean {
        return Optional.ofNullable(p0)
            .map { map -> map["miljø"] }
            .map { env: String? -> listOf(*env!!.split(",").toTypedArray()).contains(environment) }
            .orElse(false)
    }
}
