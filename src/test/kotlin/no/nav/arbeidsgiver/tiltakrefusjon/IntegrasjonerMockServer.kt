package no.nav.arbeidsgiver.tiltakrefusjon

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.common.ConsoleNotifier
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import lombok.extern.slf4j.Slf4j
import org.springframework.beans.factory.DisposableBean
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Profile("wiremock")
@Slf4j
@Component
class IntegrasjonerMockServer(@Value("\${wiremock.port}") val wiremockPort: Int) : DisposableBean {
    private val server: WireMockServer = WireMockServer(WireMockConfiguration
        .options()
        .usingFilesUnderClasspath(".")
        //.notifier(ConsoleNotifier(true))
        .port(wiremockPort))

    init {
        server.start()
    }

    override fun destroy() {
        server.stop()
    }
}