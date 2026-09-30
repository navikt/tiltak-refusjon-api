package no.nav.arbeidsgiver.tiltakrefusjon

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.common.ConsoleNotifier
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

/**
 * WireMock med stubs fra src/test/resources/mappings, for tester, lokal kjøring og labs.
 * Serveren deles mellom alle Spring-kontekstene i samme JVM og stoppes når JVM-en avslutter.
 */
@Profile("local", "wiremock")
@Component
class WireMockStubServer(@Value("\${wiremock.server.port}") port: Int) {
    val server: WireMockServer = startetServer(port)

    companion object {
        private var server: WireMockServer? = null

        @Synchronized
        private fun startetServer(port: Int): WireMockServer = server ?: WireMockServer(
            WireMockConfiguration.options()
                .port(port)
                // JDK HttpClient (RestTemplate i Boot 4) ber om h2c-oppgradering, og Jetty mister da POST-bodyen
                .http2PlainDisabled(true)
                .usingFilesUnderClasspath(".")
                .notifier(ConsoleNotifier(false))
        ).also {
            it.start()
            Runtime.getRuntime().addShutdownHook(Thread(it::stop))
            server = it
        }
    }
}
