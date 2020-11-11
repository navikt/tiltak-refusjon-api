package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import no.nav.arbeidsgiver.tiltakrefusjon.altinn.AltinnTilgangsstyringService
import no.nav.arbeidsgiver.tiltakrefusjon.autorisering.InnloggetArbeidsgiver
import no.nav.arbeidsgiver.tiltakrefusjon.autorisering.InnloggetBruker
import no.nav.arbeidsgiver.tiltakrefusjon.autorisering.InnloggetBrukerService
import no.nav.arbeidsgiver.tiltakrefusjon.autorisering.InnloggetSaksbehandler
import no.nav.arbeidsgiver.tiltakrefusjon.inntekt.InntektskomponentConsumer
import no.nav.security.token.support.core.api.Protected
import no.nav.security.token.support.spring.validation.interceptor.JwtTokenUnauthorizedException
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.HttpStatusCodeException
import java.time.LocalDate
import javax.servlet.http.HttpServletResponse

const val REQUEST_MAPPING = "/api/refusjon"

@RestController
@RequestMapping(REQUEST_MAPPING)
@Protected
class RefusjonController(val refusjonRepository: RefusjonRepository,
                         val innloggetBrukerService: InnloggetBrukerService,
                         val inntektskomponentConsumer: InntektskomponentConsumer,
                         val altinnTilgangsstyringService: AltinnTilgangsstyringService,
                         val abacTilgangsstyringService: AbacTilgangsstyringService) {

    @GetMapping("/beregn/grunnlag")
    fun beregn(grunnlag: Refusjonsgrunnlag): Int {
        return grunnlag.hentBeregnetGrunnlag()
    }

    @GetMapping
    fun hentAlle(): List<Refusjon> {
        val innloggetBruker = hentInnloggetBruker();
        return innloggetBruker.finnAlle();
    }

    //TODO: Riktig Request objekt?
    @PostMapping("/beregn")
    fun hentBeregnetRefusjonForPeriodeDeltakerOgBedrift(@RequestBody refusjonsberegningRequest: RefusjonsberegningRequest): List<Refusjonsgrunnlag> {
        //TODO: Flere refusjoner for gitt periode, bedrift og deltaker?
        val refusjon:List<Refusjon> =  refusjonRepository.findByDeltakerFnrAndBedriftnummerAndFraDatoGreaterThanEqualAndTilDatoLessThanEqual(refusjonsberegningRequest.fnr!!, refusjonsberegningRequest.bedriftNr!!, refusjonsberegningRequest.refusjonFraDato!!, refusjonsberegningRequest.refusjonTilDato!!)
        return refusjon
                .map{
                    val inntekter = inntektskomponentConsumer.hentInntekter(refusjonsberegningRequest.fnr!!,refusjonsberegningRequest.bedriftNr!!,refusjonsberegningRequest.refusjonFraDato,refusjonsberegningRequest.refusjonTilDato)
                    Refusjonsgrunnlag(inntekter,it.stillingsprosent,it.fraDato,it.tilDato,it.satsArbeidsgiveravgift,it.satsFeriepenger,0.2)
                }
    }

    @GetMapping("/deltaker/{deltakerfnr}/bedrift/{bedriftnummer}/fra/{datoFom}/til/{datoTom}")
    fun hentRefusjonForPeriodeDeltakerOgBedrift(@PathVariable deltakerfnr: String,@PathVariable bedriftnummer: String,@PathVariable datoFom: String,@PathVariable datoTom: String): List<Refusjon> {
        return  return refusjonRepository.findByDeltakerFnrAndBedriftnummerAndFraDatoGreaterThanEqualAndTilDatoLessThanEqual(deltakerfnr, bedriftnummer, LocalDate.parse(datoFom), LocalDate.parse(datoTom))
    }


    @GetMapping("/bedrift/{bedriftnummer}")
    fun hentAlleMedBedriftnummer(@PathVariable bedriftnummer: String): List<Refusjon> {
        val innloggetBruker = hentInnloggetBruker();
        return innloggetBruker.finnAlleMedBedriftnummer(bedriftnummer)
    }

    @GetMapping("/{id}")
    fun hent(@PathVariable id: String): Refusjon? {
        val innloggetBruker = hentInnloggetBruker()
        val refusjon = innloggetBruker.finnRefusjon(id) ?: throw HttpClientErrorException(HttpStatus.NO_CONTENT)
        return refusjon;
    }

    @PostMapping
    fun opprett(@RequestBody refusjon: Refusjon): Refusjon { //TODO Hvem kan opprette?
        return refusjonRepository.save(refusjon)
    }

    @PutMapping
    fun oppdater(@RequestBody refusjon: Refusjon): Refusjon {
        refusjonRepository.findByIdOrNull(refusjon.id)
                ?: throw HttpClientErrorException(HttpStatus.BAD_REQUEST, "Prøver å oppdatere en refusjon som ikke finnes")
        return refusjonRepository.save(refusjon)
    }

    private fun hentInnloggetBruker(): InnloggetBruker {
        val ident = innloggetBrukerService.hentInnloggetIdent()
        if (ident is Fnr) {
            return InnloggetArbeidsgiver(ident.verdi, altinnTilgangsstyringService, refusjonRepository)
        }
        return InnloggetSaksbehandler(ident.verdi, abacTilgangsstyringService, refusjonRepository)
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
