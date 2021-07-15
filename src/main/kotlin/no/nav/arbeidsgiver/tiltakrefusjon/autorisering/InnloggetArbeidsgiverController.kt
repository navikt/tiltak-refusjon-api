package no.nav.arbeidsgiver.tiltakrefusjon.autorisering

import io.micrometer.core.instrument.MeterRegistry
import no.nav.security.token.support.core.api.Protected
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

const val REQUEST_MAPPING_INNLOGGET_ARBEIDSGIVER = "/api/arbeidsgiver/innlogget-bruker"

@RestController
@RequestMapping(REQUEST_MAPPING_INNLOGGET_ARBEIDSGIVER)
@Protected
class InnloggetArbeidsgiverController(val innloggetBrukerService: InnloggetBrukerService, val meterRegistry: MeterRegistry) {
    init {
        meterRegistry.counter("tiltak-refusjon.hent-innlogget-arbeidsgiver").increment(0.0)
    }

    @GetMapping
    fun hentInnloggetArbeidsgiver(): InnloggetArbeidsgiver {
        meterRegistry.counter("tiltak-refusjon.hent-innlogget-arbeidsgiver").increment()
        return innloggetBrukerService.hentInnloggetArbeidsgiver()
    }
}