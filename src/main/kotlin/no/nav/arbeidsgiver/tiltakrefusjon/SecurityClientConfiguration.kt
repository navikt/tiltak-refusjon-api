package no.nav.arbeidsgiver.tiltakrefusjon

import no.nav.arbeidsgiver.tiltakrefusjon.okonomi.KontoregisterProperties
import no.nav.security.token.support.client.core.ClientProperties
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenService
import no.nav.security.token.support.client.spring.ClientConfigurationProperties
import no.nav.security.token.support.client.spring.oauth2.ClientConfigurationPropertiesMatcher
import no.nav.security.token.support.client.spring.oauth2.EnableOAuth2Client
import no.nav.security.token.support.client.spring.oauth2.OAuth2ClientRequestInterceptor
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpRequest
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.web.client.RestTemplate
import java.net.URI
import java.util.*

@EnableOAuth2Client(cacheEnabled = true)
@Configuration
@Profile("dev-gcp", "prod-gcp")
class SecurityClientConfiguration(
    val properties: KontoregisterProperties,
    val restTemplateBuilder: RestTemplateBuilder,
    val clientConfigurationProperties: ClientConfigurationProperties,
    val oAuth2AccessTokenService: OAuth2AccessTokenService
) {

    val log: Logger = LoggerFactory.getLogger(javaClass)

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
        when(registration) {
            "sokos-kontoregister" -> {
                val restTemplate: RestTemplate = restTemplateBuilder
                    .additionalInterceptors(
                        bearerTokenInterceptorKontoregister(
                            clientProperties,
                            oAuth2AccessTokenService
                        )
                    )
                    .build()
                return restTemplate
            }
            else -> {
                val restTemplate: RestTemplate = restTemplateBuilder
                    .additionalInterceptors(
                        bearerTokenInterceptor(
                            clientProperties,
                            oAuth2AccessTokenService
                        )
                    )
                    .build()
                return restTemplate
            }

        }
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

    private fun bearerTokenInterceptorKontoregister(
            clientProperties: ClientProperties,
            oAuth2AccessTokenService: OAuth2AccessTokenService
    ): ClientHttpRequestInterceptor {
        return ClientHttpRequestInterceptor { request: HttpRequest, body: ByteArray, execution: ClientHttpRequestExecution ->
            log.warn("#### OAUTH CALL: ${clientProperties.tokenEndpointUrl} ### ${clientProperties.scope} ### ${clientProperties.grantType}");
            val response = oAuth2AccessTokenService.getAccessToken(clientProperties) //todo feil her!!!!
            request.headers.setBearerAuth(response.accessToken!!)
            request.headers.set("Nav-Consumer-Id",properties.consumerId)
            request.headers.set("Nav-Call-Id",UUID.randomUUID().toString())
            log.warn("#### HEADERS: ${request.headers}");
            execution.execute(request, body)
        }
    }
}