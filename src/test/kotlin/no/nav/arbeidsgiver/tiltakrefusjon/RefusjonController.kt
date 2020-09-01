package no.nav.arbeidsgiver.tiltakrefusjon

import no.nav.arbeidsgiver.tiltakrefusjon.domain.Refusjon
import no.nav.arbeidsgiver.tiltakrefusjon.domain.Refusjonsgrunnlag
import no.nav.security.token.support.core.api.Unprotected
import no.nav.security.token.support.spring.validation.interceptor.JwtTokenUnauthorizedException
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.HttpStatusCodeException
import java.math.BigDecimal
import javax.servlet.http.HttpServletResponse

const val REQUEST_MAPPING = "/refusjon"

@RestController
@RequestMapping(REQUEST_MAPPING)
@Unprotected
class RefusjonController(val refusjonRepository: RefusjonRepository) {
    @GetMapping("/beregn")
    fun beregn(grunnlag: Refusjonsgrunnlag): BigDecimal {
        return beregnRefusjon(grunnlag)
    }

    @GetMapping
    fun hentAlle(): List<Refusjon> {
        return refusjonRepository.findAll()
    }

    @GetMapping("/{id}")
    fun hent(@PathVariable id: String): Refusjon? {
        return refusjonRepository.findByIdOrNull(id)
    }

    @PutMapping
    fun oppdater(@RequestBody refusjon: Refusjon): Refusjon {
        refusjonRepository.findByIdOrNull(refusjon.id) ?: throw HttpClientErrorException(HttpStatus.BAD_REQUEST, "Prøver å oppdatere en refusjon som ikke finnes")
        return refusjonRepository.save(refusjon)
    }

    @ExceptionHandler
    fun håndterException(e : Exception, response : HttpServletResponse){

        if (e is HttpStatusCodeException) {
            response.sendError(e.statusCode.value(), e.statusText)
            return
        }
        if(e is JwtTokenUnauthorizedException){
            response.sendError(HttpStatus.UNAUTHORIZED.value(), e.message)
            return
        }
        response.sendError(HttpStatus.SERVICE_UNAVAILABLE.value(), e.message)
    }
}
