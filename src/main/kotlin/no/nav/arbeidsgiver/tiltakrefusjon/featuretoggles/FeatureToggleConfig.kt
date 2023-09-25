package no.nav.arbeidsgiver.tiltakrefusjon.featuretoggles

import io.getunleash.DefaultUnleash
import io.getunleash.Unleash
import io.getunleash.util.UnleashConfig
import jakarta.servlet.http.HttpServletRequest

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.context.annotation.RequestScope

@Configuration
class FeatureToggleConfig {
    @Bean
    @ConditionalOnProperty("tiltak-refusjon.unleash.enabled")
    fun initializeUnleash(
        @Value("\${tiltak-refusjon.unleash.api-uri}") unleashUrl: String?,
        @Value("\${tiltak-refusjon.unleash.api-token}") apiKey: String,
        byEnvironmentStrategy: ByEnvironmentStrategy
    ): Unleash {
        val config = UnleashConfig.builder()
            .appName(APP_NAME)
            .instanceId(APP_NAME + "-" + byEnvironmentStrategy.environment)
            .unleashAPI(unleashUrl!!)
            .apiKey(apiKey)
            .build()
        return DefaultUnleash(
            config,
            byEnvironmentStrategy
        )
    }

    @Bean
    @ConditionalOnProperty("tiltak-refusjon.unleash.mock")
    @RequestScope
    fun unleashMock(@Autowired request: HttpServletRequest): Unleash {
        val fakeUnleash = FakeFakeUnleash()
        val allEnabled = "enabled" == request.getHeader("features")
        if (allEnabled) {
            fakeUnleash.enableAll()
        } else {
            fakeUnleash.disableAll()
        }
        return fakeUnleash
    }

    companion object {
        private const val APP_NAME = "tiltak-refusjon-api"
    }
}