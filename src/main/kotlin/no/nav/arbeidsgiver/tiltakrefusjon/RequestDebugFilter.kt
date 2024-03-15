package no.nav.arbeidsgiver.tiltakrefusjon

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Order(Ordered.HIGHEST_PRECEDENCE + 5)
@Component
class RequestDebugFilter: OncePerRequestFilter() {
    val log = LoggerFactory.getLogger(javaClass)
    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain) {
        if (request.getHeader("if-unmodified-since") != null) {
            log.info("if-unmodified-since fra header: ${request.getHeader("if-unmodified-since")}")
        }
        filterChain.doFilter(request, response)
    }
}
