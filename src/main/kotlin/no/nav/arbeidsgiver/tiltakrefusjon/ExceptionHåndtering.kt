package no.nav.arbeidsgiver.tiltakrefusjon

import no.nav.arbeidsgiver.tiltakrefusjon.autorisering.TilgangskontrollException
import no.nav.security.token.support.core.exceptions.JwtTokenInvalidClaimException
import no.nav.security.token.support.core.exceptions.JwtTokenMissingException
import no.nav.security.token.support.core.exceptions.JwtTokenValidatorException
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

@ControllerAdvice
class ExceptionHandler : ResponseEntityExceptionHandler() {
    @ExceptionHandler(FeilkodeException::class)
    fun håndterFeilkodeException(e: FeilkodeException, request: WebRequest): ResponseEntity<Nothing> {
        val headers = HttpHeaders()
        headers["feilkode"] = e.feilkode.toString()
        return ResponseEntity(null, headers, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(JwtTokenInvalidClaimException::class, JwtTokenMissingException::class, JwtTokenValidatorException::class)
    fun håndterAutentisering(e: Exception, request: WebRequest): ResponseEntity<Nothing> {
        return ResponseEntity(HttpStatus.UNAUTHORIZED)
    }

    @ExceptionHandler(TilgangskontrollException::class)
    fun håndterAutorisering(e: TilgangskontrollException, request: WebRequest): ResponseEntity<Nothing> {
        val headers = HttpHeaders()
        headers["feilkode"] = e.feilkode.toString()
        return ResponseEntity(null, headers, HttpStatus.FORBIDDEN)
    }

    @ExceptionHandler(UgyldigRequestException::class)
    fun håndterUgyldigRequest(e: Exception, request: WebRequest): ResponseEntity<Nothing> {
        return ResponseEntity(HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(RessursFinnesIkkeException::class)
    fun håndter404(e: Exception, request: WebRequest): ResponseEntity<Nothing> {
        return ResponseEntity(HttpStatus.NOT_FOUND)
    }
}

/**
 * Exception som mappes til statuskode 404. Brukes når man prøver å hente en ressurs med en id som ikke eksisterer.
 * Brukes også om man forespør en ressurs som eksisterer, men som man ikke skal vite at eksisterer.
 */
class RessursFinnesIkkeException : RuntimeException()

/**
 * Exception som mappes til statuskode 400. Kastes når det teknisk ikke er en gyldig request.
 * Hvis det skal vises en feilmelding i frontend er det no.nav.arbeidsgiver.tiltakrefusjon.FeilkodeException som skal brukes.
 */
class UgyldigRequestException : RuntimeException()
