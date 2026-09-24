package no.nav.arbeidsgiver.tiltakrefusjon

import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource

abstract class WireMockTestSupport {
    companion object {
        @JvmStatic
        @DynamicPropertySource
        fun registerWireMockProperties(registry: DynamicPropertyRegistry) {
            registry.add("wiremock.server.port") { WireMockServerHolder.port() }
        }
    }
}
