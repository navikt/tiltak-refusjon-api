package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import no.nav.arbeidsgiver.tiltakrefusjon.autorisering.InnloggetBrukerService
import no.nav.security.token.support.core.api.Protected
import no.nav.security.token.support.spring.validation.interceptor.JwtTokenUnauthorizedException
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.HttpStatusCodeException
import javax.servlet.http.HttpServletResponse

const val REQUEST_MAPPING_ARBEIDSGIVER_REFUSJON = "/api/arbeidsgiver/refusjon"

data class HentArbeidsgiverRefusjonerQueryParametre(val bedriftNr: String, val status: RefusjonStatus? = null, val tiltakstype: Tiltakstype? = null)

@RestController
@RequestMapping(REQUEST_MAPPING_ARBEIDSGIVER_REFUSJON)
@Protected
class ArbeidsgiverRefusjonController(
        val innloggetBrukerService: InnloggetBrukerService
) {
    @GetMapping
    fun hentAlle(queryParametre: HentArbeidsgiverRefusjonerQueryParametre): List<Refusjon> {
        val arbeidsgiver = innloggetBrukerService.hentInnloggetArbeidsgiver()
        return arbeidsgiver.finnAlleMedBedriftnummer(queryParametre.bedriftNr)
                .filter { queryParametre.status == null || queryParametre.status == it.status }
                .filter { queryParametre.tiltakstype == null || queryParametre.tiltakstype == it.tilskuddsgrunnlag.tiltakstype }
    }

    @GetMapping("/{id}")
    fun hent(@PathVariable id: String): Refusjon? {
        val arbeidsgiver = innloggetBrukerService.hentInnloggetArbeidsgiver()
        return arbeidsgiver.finnRefusjon(id) ?: throw HttpClientErrorException(HttpStatus.NO_CONTENT)
    }

    @PostMapping("/{id}/inntektsoppslag")
    fun gjørInntektsoppslag(@PathVariable id: String) {
        val arbeidsgiver = innloggetBrukerService.hentInnloggetArbeidsgiver()
        arbeidsgiver.gjørInntektsoppslag(id)
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
