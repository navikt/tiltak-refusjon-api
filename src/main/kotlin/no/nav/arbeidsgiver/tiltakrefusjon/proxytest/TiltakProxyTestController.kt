package no.nav.arbeidsgiver.tiltakrefusjon.proxytest

import no.nav.arbeidsgiver.tiltakrefusjon.utils.ConditionalOnPropertyNotEmpty
import no.nav.security.token.support.core.api.Protected
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.RestTemplate


@RestController
@RequestMapping("/api/proxytest")
@Protected
@ConditionalOnPropertyNotEmpty("no.nav.security.jwt.client")
class TiltakProxyTestController(
        @Qualifier("tokenx") val restTemplate: RestTemplate
) {
    @GetMapping("")
    fun test(): String? {
        return restTemplate.getForObject("https://tiltak-proxy.dev-fss-pub.nais.io/test", String::class.java);
    }
}