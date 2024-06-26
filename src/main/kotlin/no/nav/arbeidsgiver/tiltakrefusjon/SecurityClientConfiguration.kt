package no.nav.arbeidsgiver.tiltakrefusjon

import no.nav.security.token.support.client.core.ClientProperties
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenService
import no.nav.security.token.support.client.spring.ClientConfigurationProperties
import no.nav.security.token.support.client.spring.oauth2.EnableOAuth2Client
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpRequest
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.web.client.RestTemplate

@EnableOAuth2Client(cacheEnabled = true)
@Configuration
@Profile("dev-gcp", "prod-gcp")
class SecurityClientConfiguration(
        val restTemplateBuilder: RestTemplateBuilder,
        val clientConfigurationProperties: ClientConfigurationProperties,
        val oAuth2AccessTokenService: OAuth2AccessTokenService
) {

    @Bean
    fun påVegneAvSaksbehandlerGraphRestTemplate() = restTemplateForRegistration("aad-graph")

    @Bean
    fun påVegneAvSaksbehandlerProxyRestTemplate() = restTemplateForRegistration("aad")

    @Bean
    fun påVegneAvArbeidsgiverAltinnRestTemplate() = restTemplateForRegistration("tokenx-altinn")

    @Bean
    fun anonymProxyRestTemplate() = restTemplateForRegistration("aad-anonym")

    @Bean("sokosRestTemplate")
    fun sokosRestTemplate() = restTemplateForRegistration("sokos-kontoregister")


    private fun restTemplateForRegistration(registration: String): RestTemplate {
        val clientProperties = clientConfigurationProperties.registration[registration]
                ?: throw RuntimeException("could not find oauth2 client config for $registration")
        return restTemplateBuilder
                .additionalInterceptors(bearerTokenInterceptor(clientProperties, oAuth2AccessTokenService))
                .build()
    }

    private fun bearerTokenInterceptor(
            clientProperties: ClientProperties,
            oAuth2AccessTokenService: OAuth2AccessTokenService
    ): ClientHttpRequestInterceptor {
        return ClientHttpRequestInterceptor { request: HttpRequest, body: ByteArray, execution: ClientHttpRequestExecution ->
            val response = oAuth2AccessTokenService.getAccessToken(clientProperties)
            request.headers.setBearerAuth(response.accessToken!!)
            execution.execute(request, body)
        }
    }
}