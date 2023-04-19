package no.nav.arbeidsgiver.tiltakrefusjon

import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
@Profile("dev-gcp-labs")
class LabsSecurityClientConfiguration(val restTemplateBuilder: RestTemplateBuilder) {
    @Bean
    fun påVegneAvSaksbehandlerGraphRestTemplate() = restTemplateBuilder.build()

    @Bean
    fun påVegneAvSaksbehandlerProxyRestTemplate() = restTemplateBuilder.build()

    @Bean
    fun påVegneAvArbeidsgiverAltinnRestTemplate() = restTemplateBuilder.build()

    @Bean
    fun anonymProxyRestTemplate() = restTemplateBuilder.build()
}