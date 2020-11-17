package no.nav.arbeidsgiver.tiltakrefusjon.proxytest

import no.nav.security.token.support.core.api.Protected
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Profile
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.RestTemplate


@RestController
@RequestMapping("/api/proxytest")
@Protected
@Profile("dev-gcp")
class TiltakProxyTestController(
        @Qualifier("påVegneAvArbeidsgiverProxyRestTemplate")
        val påVegneAvArbeidsgiverProxyRestTemplate: RestTemplate,
        @Qualifier("påVegneAvSaksbehandlerProxyRestTemplate")
        val påVegneAvSaksbehandlerProxyRestTemplate: RestTemplate,
        @Qualifier("anonymProxyRestTemplate")
        val anonymProxyRestTemplate: RestTemplate
) {
    @GetMapping("/tokenx")
    fun testTokenX(): String? {
        return påVegneAvArbeidsgiverProxyRestTemplate.getForObject("https://tiltak-proxy.dev-fss-pub.nais.io/test-tokenx", String::class.java)
    }

    @GetMapping("/aad")
    fun testAad(): String? {
        return påVegneAvSaksbehandlerProxyRestTemplate.getForObject("https://tiltak-proxy.dev-fss-pub.nais.io/test-aad", String::class.java)
    }

    @GetMapping("/aad-anonym")
    fun testAadAnonym(): String? {
        return anonymProxyRestTemplate.getForObject("https://tiltak-proxy.dev-fss-pub.nais.io/test-aad", String::class.java)
    }
}