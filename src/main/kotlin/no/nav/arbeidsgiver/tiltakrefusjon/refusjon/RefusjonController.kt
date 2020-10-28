package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import no.nav.arbeidsgiver.tiltakrefusjon.autorisering.InnloggetSaksbehandlerService
import no.nav.arbeidsgiver.tiltakrefusjon.autorisering.InnloggingService
import no.nav.security.token.support.core.api.Protected
import no.nav.security.token.support.spring.validation.interceptor.JwtTokenUnauthorizedException
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.HttpStatusCodeException
import java.math.BigDecimal
import javax.servlet.http.HttpServletResponse

const val REQUEST_MAPPING = "/api/refusjon"

@RestController
@RequestMapping(REQUEST_MAPPING)
@Protected

class RefusjonController(val refusjonRepository: RefusjonRepository,
                         val innloggingService: InnloggingService,
                         val innloggetSaksbehandlerService: InnloggetSaksbehandlerService) {

    @GetMapping("/beregn")
    fun beregn(grunnlag: Refusjonsgrunnlag): BigDecimal {
        return beregnRefusjon(grunnlag)
    }

    @GetMapping
    fun hentAlle(): List<Refusjon> {
        val innloggetSaksbehandler = innloggetSaksbehandlerService.hentInnloggetSaksbehandler()
        return refusjonRepository.findAll()
                .filter { refusjon: Refusjon ->
                    innloggetSaksbehandlerService.harLeseTilgang(innloggetSaksbehandler.identifikator, refusjon.deltakerFnr)
                }
    }

    @GetMapping("/bedrift/{bedriftnummer}")
    fun hentAlleMedBedriftnummer(@PathVariable bedriftnummer: String): List<Refusjon> {
        innloggingService.sjekkHarTilgangTilRefusjonerForBedrift(bedriftnummer)
        return refusjonRepository.findByBedriftnummer(bedriftnummer)
    }

    @GetMapping("/{id}")
    fun hent(@PathVariable id: String): Refusjon? {
        return refusjonRepository.findByIdOrNull(id)
    }

    @PostMapping
    fun opprett(@RequestBody refusjon: Refusjon): Refusjon {
        return refusjonRepository.save(refusjon)
    }

    @PutMapping
    fun oppdater(@RequestBody refusjon: Refusjon): Refusjon {
        refusjonRepository.findByIdOrNull(refusjon.id)
                ?: throw HttpClientErrorException(HttpStatus.BAD_REQUEST, "Prøver å oppdatere en refusjon som ikke finnes")
        return refusjonRepository.save(refusjon)
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
