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
        @Qualifier("tokenx") val restTemplate: RestTemplate
) {
    @GetMapping("")
    fun test(): String? {
        return restTemplate.getForObject("https://tiltak-proxy.dev-fss-pub.nais.io/test", String::class.java);
    }
}