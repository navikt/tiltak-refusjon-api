package no.nav.arbeidsgiver.tiltakrefusjon.metrikker

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.events.GodkjentAvArbeidsgiver
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.ZoneId

@Component
class Metrikker(
    val meterRegistry: MeterRegistry
) {

    @EventListener
    fun refusjonGodkjentAvArbeidsgiver(event: GodkjentAvArbeidsgiver) {
        var timer = Timer.builder("tiltak-refusjon.tidsbruk-sende-refusjon")
            .description("Tid brukt for å sende inn refusjon")
            .register(meterRegistry)

        val åpnetFørsteGang = event.refusjon.åpnetFørsteGang
        val godkjentAvArbeidsgiver = event.refusjon.godkjentAvArbeidsgiver
        if(åpnetFørsteGang != null && godkjentAvArbeidsgiver != null) {
            val tidBrukt = Duration.between(åpnetFørsteGang, godkjentAvArbeidsgiver)
            timer.record(tidBrukt)
        }
    }

}
