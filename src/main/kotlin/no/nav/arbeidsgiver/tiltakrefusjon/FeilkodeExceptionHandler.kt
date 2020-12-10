package no.nav.arbeidsgiver.tiltakrefusjon

import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.FeilkodeException
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import javax.servlet.http.HttpServletResponse

@ControllerAdvice
class FeilkodeExceptionHandler : ResponseEntityExceptionHandler() {
    @ExceptionHandler(*[FeilkodeException::class])
    fun håndterException(e: FeilkodeException, response: HttpServletResponse) {
        response.setHeader("feilkode", e.feilkode.toString())
        response.sendError(HttpStatus.BAD_REQUEST.value(), e.message)
    }
}