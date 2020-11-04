package no.nav.arbeidsgiver.tiltakrefusjon

import no.nav.arbeidsgiver.tiltakrefusjon.utils.ConditionalOnPropertyNotEmpty
import no.nav.security.token.support.client.core.ClientProperties
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenService
import no.nav.security.token.support.client.spring.ClientConfigurationProperties
import no.nav.security.token.support.client.spring.oauth2.EnableOAuth2Client
import no.nav.security.token.support.spring.api.EnableJwtTokenValidation
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpRequest
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.web.client.RestTemplate

@EnableOAuth2Client(cacheEnabled = true)
@Configuration
@ConditionalOnPropertyNotEmpty("no.nav.security.jwt.client")
class SecurityClientConfiguration {
    /**
     * Create one RestTemplate per OAuth2 client entry to separate between different scopes per API
     */
    @Bean("azure")
    fun downstreamResourceRestTemplate(
            restTemplateBuilder: RestTemplateBuilder,
            clientConfigurationProperties: ClientConfigurationProperties,
            oAuth2AccessTokenService: OAuth2AccessTokenService
    ): RestTemplate? {
        val clientProperties = clientConfigurationProperties.registration["aad"]
                ?: throw RuntimeException("could not find oauth2 client config for aad")
        return restTemplateBuilder
                .additionalInterceptors(bearerTokenInterceptor(clientProperties, oAuth2AccessTokenService))
                .build()
    }

    @Bean("tokenx")
    fun downstreamResourceTokenXRestTemplate(
            restTemplateBuilder: RestTemplateBuilder,
            clientConfigurationProperties: ClientConfigurationProperties,
            oAuth2AccessTokenService: OAuth2AccessTokenService
    ): RestTemplate? {
        val clientProperties = clientConfigurationProperties.registration["tokenx"]
                ?: throw RuntimeException("could not find oauth2 client config for tokenx")
        return restTemplateBuilder
                .additionalInterceptors(bearerTokenInterceptor(clientProperties, oAuth2AccessTokenService))
                .build()
    }

    private fun bearerTokenInterceptor(
            clientProperties: ClientProperties,
            oAuth2AccessTokenService: OAuth2AccessTokenService
    ): ClientHttpRequestInterceptor? {
        return ClientHttpRequestInterceptor { request: HttpRequest, body: ByteArray?, execution: ClientHttpRequestExecution ->
            val response = oAuth2AccessTokenService.getAccessToken(clientProperties)
            request.headers.setBearerAuth(response.accessToken)
            execution.execute(request, body!!)
        }
    }
}