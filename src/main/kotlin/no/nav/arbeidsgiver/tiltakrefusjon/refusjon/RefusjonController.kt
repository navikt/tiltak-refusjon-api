package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import no.nav.arbeidsgiver.tiltakrefusjon.autorisering.InnloggetBrukerService
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.nydatamodell.Refusjonsak
import no.nav.security.token.support.core.api.Protected
import no.nav.security.token.support.spring.validation.interceptor.JwtTokenUnauthorizedException
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.HttpStatusCodeException
import javax.servlet.http.HttpServletResponse

const val REQUEST_MAPPING = "/api/refusjon"

@RestController
@RequestMapping(REQUEST_MAPPING)
@Protected
class RefusjonController(
        val innloggetBrukerService: InnloggetBrukerService
) {

    @GetMapping
    fun hentAlle(): List<Refusjonsak> {
        val innloggetBruker = innloggetBrukerService.hentInnloggetBruker()
        return innloggetBruker.finnAlle()
    }

    @GetMapping("/bedrift/{bedriftnummer}")
    fun hentAlleMedBedriftnummer(@PathVariable bedriftnummer: String): List<Refusjonsak> {
        val innloggetBruker = innloggetBrukerService.hentInnloggetBruker()
        return innloggetBruker.finnAlleMedBedriftnummer(bedriftnummer)
    }

    @GetMapping("/{id}")
    fun hent(@PathVariable id: String): Refusjonsak? {
        val innloggetBruker = innloggetBrukerService.hentInnloggetBruker()
        return innloggetBruker.finnRefusjonsak(id) ?: throw HttpClientErrorException(HttpStatus.NO_CONTENT)
    }

    @ExceptionHandler
    fun håndterException(e: Exception, response: HttpServletResponse) {
        if (e is HttpStatusCodeException) {
            response.sendError(e.statusCode.value(), e.statusText)
            return
        }
        if (e is JwtTokenUnauthorizedException) {
            response.sendError(HttpStatus.UNAUTHORIZED.value(), e.message)
            return
        }
        response.sendError(HttpStatus.SERVICE_UNAVAILABLE.value(), e.message)
    }
}
