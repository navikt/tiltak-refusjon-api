package no.nav.arbeidsgiver.tiltakrefusjon.featuretoggles;

import no.finn.unleash.DefaultUnleash;
import no.finn.unleash.Unleash;
import no.finn.unleash.util.UnleashConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.annotation.RequestScope;

import javax.servlet.http.HttpServletRequest;


@Configuration
public class FeatureToggleConfig {

    private static final String APP_NAME = "tiltak-refusjon-api";

    @Bean
    @ConditionalOnProperty("tiltak-refusjon.unleash.enabled")
    public Unleash initializeUnleash(@Value(
            "${tiltak-refusjon.unleash.unleash-uri}") String unleashUrl,
                                     ByEnvironmentStrategy byEnvironmentStrategy) {
        UnleashConfig config = UnleashConfig.builder()
                .appName(APP_NAME)
                .instanceId(APP_NAME + "-" + byEnvironmentStrategy.getEnvironment())
                .unleashAPI(unleashUrl)
                .build();

        return new DefaultUnleash(
                config,
                byEnvironmentStrategy
        );
    }

    @Bean
    @ConditionalOnProperty("tiltak-refusjon.unleash.mock")
    @RequestScope
    public Unleash unleashMock(@Autowired HttpServletRequest request) {
        FakeFakeUnleash fakeUnleash = new FakeFakeUnleash();
        boolean allEnabled = "enabled".equals(request.getHeader("features"));
        if (allEnabled) {
            fakeUnleash.enableAll();
        } else {
            fakeUnleash.disableAll();
        }
        return fakeUnleash;
    }
}
