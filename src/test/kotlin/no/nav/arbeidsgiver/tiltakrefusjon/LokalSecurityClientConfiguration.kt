package no.nav.arbeidsgiver.tiltakrefusjon

import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class LokalSecurityClientConfiguration(val restTemplateBuilder: RestTemplateBuilder) {
    @Bean
    fun påVegneAvSaksbehandlerProxyRestTemplate() = restTemplateBuilder.build()

    @Bean
    fun påVegneAvArbeidsgiverProxyRestTemplate() = restTemplateBuilder.build()

    @Bean
    fun anonymProxyRestTemplate() = restTemplateBuilder.build()
}