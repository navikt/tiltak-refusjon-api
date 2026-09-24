package no.nav.arbeidsgiver.tiltakrefusjon

import org.springframework.boot.EnvironmentPostProcessor
import org.springframework.boot.SpringApplication
import org.springframework.core.env.ConfigurableEnvironment
import org.springframework.core.env.PropertySource

/**
 * Starter WireMock først når `wiremock.server.port` slås opp, og gir tilbake porten serveren faktisk lytter på.
 * Konfigurert port (f.eks. `WIREMOCK_SERVER_PORT` i labs) brukes hvis den er satt, ellers velges en ledig port.
 * Gjelder tester, LokalTiltakRefusjonApplication, DockerComposeTiltakRefusjonApplication og labs.
 */
class WireMockEnvironmentPostProcessor : EnvironmentPostProcessor {
    override fun postProcessEnvironment(environment: ConfigurableEnvironment, application: SpringApplication) {
        if (!environment.propertySources.contains(WireMockPortPropertySource.NAME)) {
            environment.propertySources.addFirst(WireMockPortPropertySource(environment))
        }
    }
}

private class WireMockPortPropertySource(
    private val environment: ConfigurableEnvironment
) : PropertySource<Any>(NAME, Any()) {

    private val resolving = ThreadLocal.withInitial { false }

    override fun getProperty(name: String): Any? {
        // Boot sin "configurationProperties"-kilde slår opp i hele miljøet igjen, også i denne kilden
        if (name != PORT_PROPERTY || resolving.get()) return null
        resolving.set(true)
        try {
            return WireMockServerHolder.port(configuredPort())
        } finally {
            resolving.set(false)
        }
    }

    private fun configuredPort(): Int =
        environment.getProperty(PORT_PROPERTY)?.toInt() ?: 0

    companion object {
        const val NAME = "wiremock"
        const val PORT_PROPERTY = "wiremock.server.port"
    }
}
