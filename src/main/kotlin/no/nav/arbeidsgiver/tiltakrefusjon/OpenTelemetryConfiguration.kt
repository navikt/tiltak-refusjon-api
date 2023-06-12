package no.nav.arbeidsgiver.tiltakrefusjon

import io.micrometer.observation.Observation
import io.micrometer.observation.ObservationPredicate
import io.micrometer.observation.ObservationRegistry
import io.micrometer.observation.aop.ObservedAspect
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.http.server.observation.ServerRequestObservationContext
import org.springframework.web.filter.ServerHttpObservationFilter

/**
 * Alt man trenger for å ha tracing i applikasjonen!
 * I application.yaml legger man til følgende:
 * - management.tracing.enabled: true
 * - management.tracing.sampling.probability: 1.0
 *
 * I nais.yaml legger man til:
 * -   spec.observability.tracing.enabled: true
 */
@Profile("!local")
@Configuration
class OpenTelemetryConfiguration {

    // Nais forventer at man bruker grpc-protokollen mot endepunktet
    @Bean
    fun otlpExporter(@Value("\${OTEL_EXPORTER_OTLP_ENDPOINT}") otelEndpoint: String): OtlpGrpcSpanExporter {
        return OtlpGrpcSpanExporter.builder()
            .setEndpoint(otelEndpoint)
            .build()
    }

    // "@Observed"-annotasjoner fungerer ikke uten denne
    // OBS: @Observed fungerer kun på spring-komponenter (@Service, @Component...)
    @Bean
    fun observedAspect(observationRegistry: ObservationRegistry) = ObservedAspect(observationRegistry)

    // Observer http-requester mot tjenesten
    @Bean
    fun httpObservationFilter(registry: ObservationRegistry) = ServerHttpObservationFilter(registry)

    // Legg til et predikat slik at kall mot internal og actuator ikke observeres
    @Bean
    fun serverContextPredicate() = ObservationPredicate { name: String, context: Observation.Context ->
        if (name == "http.server.requests" && context is ServerRequestObservationContext) {
            return@ObservationPredicate !context.carrier.requestURI.startsWith("/actuator") && !context.carrier.requestURI.startsWith("/internal")
        }
        return@ObservationPredicate true
    }
}