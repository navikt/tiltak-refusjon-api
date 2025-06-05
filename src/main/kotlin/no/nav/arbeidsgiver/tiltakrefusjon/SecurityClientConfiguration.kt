package no.nav.arbeidsgiver.tiltakrefusjon

import no.nav.security.token.support.client.core.ClientProperties
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenService
import no.nav.security.token.support.client.spring.ClientConfigurationProperties
import no.nav.security.token.support.client.spring.oauth2.EnableOAuth2Client
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.http.HttpRequest
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.web.client.RestTemplate


@Configuration
@EnableOAuth2Client(cacheEnabled = true)
class SecurityClientConfiguration(
        val restTemplateBuilder: RestTemplateBuilder,
        val clientConfigurationProperties: ClientConfigurationProperties,
        val oAuth2AccessTokenService: OAuth2AccessTokenService
) {
    @Autowired
    private val environment: Environment? = null

    @Bean
    fun påVegneAvArbeidsgiverAltinnRestTemplate() = restTemplateForRegistration("tokenx-altinn")

    @Bean
    fun anonymProxyRestTemplate() = restTemplateForRegistration("aad-anonym")

    @Bean
    fun sokosRestTemplate() = restTemplateForRegistration("sokos-kontoregister")

    @Bean
    fun ikompRestTemplate() = restTemplateForRegistration("ikomp")

    private fun restTemplateForRegistration(registration: String): RestTemplate {
        val erDevEllerProd = environment?.activeProfiles?.any {
            env -> env.equals("dev-gcp") || env.equals("prod-gcp")
        } ?: false

        if (!erDevEllerProd) {
            return restTemplateBuilder.build()
        }

        val clientProperties: ClientProperties = clientConfigurationProperties.registration[registration]
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
