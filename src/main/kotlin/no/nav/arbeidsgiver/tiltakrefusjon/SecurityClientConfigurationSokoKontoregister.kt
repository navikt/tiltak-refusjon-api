package no.nav.arbeidsgiver.tiltakrefusjon

import no.nav.security.token.support.client.core.ClientProperties
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenService
import no.nav.security.token.support.client.spring.ClientConfigurationProperties
import no.nav.security.token.support.client.spring.oauth2.ClientConfigurationPropertiesMatcher
import no.nav.security.token.support.client.spring.oauth2.EnableOAuth2Client
import no.nav.security.token.support.client.spring.oauth2.OAuth2ClientRequestInterceptor
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.web.client.RestTemplate
import java.net.URI
import java.util.*

@EnableOAuth2Client(cacheEnabled = true)
@Configuration
@Profile("dev-gcp", "prod-gcp")
class SecurityClientConfigurationSokoKontoregister{

    @Bean("sokosRestTemplate")
    fun azureRestTemplate(
        restTemplateBuilder: RestTemplateBuilder,
        oAuth2ClientRequestInterceptor: OAuth2ClientRequestInterceptor?
    ): RestTemplate {
        return restTemplateBuilder
            .interceptors(oAuth2ClientRequestInterceptor)
            .build()
    }

    @Bean
    fun oAuth2ClientRequestInterceptor(
        properties: ClientConfigurationProperties?,
        service: OAuth2AccessTokenService?,
        matcher: ClientConfigurationPropertiesMatcher?
    ): OAuth2ClientRequestInterceptor {
        return OAuth2ClientRequestInterceptor(properties!!, service!!, matcher!!)
    }

    @Bean
    fun clientConfigurationPropertiesMatcher(): ClientConfigurationPropertiesMatcher {
        return object : ClientConfigurationPropertiesMatcher {
            override fun findProperties(
                properties: ClientConfigurationProperties,
                s: String
            ): ClientProperties? {
                   return properties.registration["sokos-kontoregister"]
            }
        }
    }
}