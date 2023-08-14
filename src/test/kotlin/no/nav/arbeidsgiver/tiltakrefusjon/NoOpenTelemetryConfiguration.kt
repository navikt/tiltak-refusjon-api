package no.nav.arbeidsgiver.tiltakrefusjon

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.MDC
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.web.filter.OncePerRequestFilter
import java.util.*

@Profile("local")
@Configuration
class NoOpenTelemetryConfiguration {

    // OpenTelemetry-beans leverer traceId. I lokal modus må vi levere traceId selv.
    @Bean
    fun traceIdProvider(): OncePerRequestFilter =
        object : OncePerRequestFilter() {
            override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain) {
                try {
                    MDC.put("traceId", UUID.randomUUID().toString())
                    filterChain.doFilter(request, response)
                } finally {
                    MDC.remove("traceId")
                }
            }
        }
}