package no.nav.arbeidsgiver.tiltakrefusjon.autorisering

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import no.nav.security.token.support.core.api.Protected
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

const val REQUEST_MAPPING_INNLOGGET_ARBEIDSGIVER = "/api/arbeidsgiver"

@RestController
@RequestMapping(REQUEST_MAPPING_INNLOGGET_ARBEIDSGIVER)
@Protected
class InnloggetArbeidsgiverController(
    val innloggetBrukerService: InnloggetBrukerService,
    val meterRegistry: MeterRegistry,
) {
    val counter: Counter = meterRegistry.counter("tiltak-refusjon.hent-innlogget-arbeidsgiver")

    init {
        counter.increment(0.0)
    }

    @GetMapping("/innlogget-bruker")
    fun hentInnloggetArbeidsgiver(): InnloggetArbeidsgiver {
        counter.increment()
        return innloggetBrukerService.hentInnloggetArbeidsgiver()
    }
}
