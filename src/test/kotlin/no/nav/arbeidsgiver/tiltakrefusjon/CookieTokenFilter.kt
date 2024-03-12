package no.nav.arbeidsgiver.tiltakrefusjon

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletRequestWrapper
import jakarta.servlet.http.HttpServletResponse
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

/**
 * Dette filteret gjør at lokalkjøring av applikasjonen fortsatt fungerer med cookie-baserte
 * jwt-tokens. Frontendene har en lokal login-mekanisme når man kjører i dev-modus som henter
 * et fake JWT access-token og legger det i en cookie.
 *
 * Tidligere ble denne cookien plukket opp av token-validation-biblioteket, men siden versjon 4
 * har støtte for cookies blitt fjernet.
 *
 * Ved å sørge for at dette filteret kjører før TokenValidationFilter sniker vi inn en auth-header
 * som deretter plukkes opp av token-validering senere.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class CookieTokenFilter: OncePerRequestFilter() {
    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain) {
        val aadToken = request.cookies.find { it.name == "aad-token" }?.value
        val tokenxToken = request.cookies.find { it.name == "tokenx-token" }?.value

        if (request.requestURI.startsWith("/api/arbeidsgiver") && tokenxToken != null) {
            val wrappedRequest = RequestMedToken(request, tokenxToken)
            filterChain.doFilter(wrappedRequest, response)
        } else if (request.requestURI.startsWith("/api/saksbehandler") && aadToken != null) {
            val wrappedRequest = RequestMedToken(request, aadToken)
            filterChain.doFilter(wrappedRequest, response)
        } else {
            filterChain.doFilter(request, response);
        }
    }
}

private class RequestMedToken(request: HttpServletRequest, val token: String): HttpServletRequestWrapper(request) {
    override fun getHeader(name: String?): String? {
        if (name?.lowercase() == "authorization") {
            return "Bearer $token"
        }
        return super.getHeader(name)
    }
}
