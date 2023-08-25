package no.nav.arbeidsgiver.tiltakrefusjon

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.MDC
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.util.*

const val CALL_ID_HEADER = "Nav-Call-Id"
const val MDC_CALL_ID_KEY = "callId"

@Order(Ordered.HIGHEST_PRECEDENCE + 1)
@Component
class CallIdFilter : OncePerRequestFilter() {
    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain) {
        try {
            val callId = request.getHeader(CALL_ID_HEADER) ?: UUID.randomUUID().toString()

            MDC.put(MDC_CALL_ID_KEY, callId)
            request.setAttribute(CALL_ID_HEADER, callId)
            response.setHeader(CALL_ID_HEADER, callId)
            filterChain.doFilter(request, response)
        } finally {
            MDC.remove(MDC_CALL_ID_KEY)
        }
    }
}