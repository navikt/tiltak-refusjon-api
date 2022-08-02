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

        val inntektsgrunnlagInnhentet =
            event.refusjon.inntektsgrunnlag?.innhentetTidspunkt?.atZone(ZoneId.of("Europe/Oslo"))?.toInstant()
        val godkjentAvArbeidsgiver = event.refusjon.godkjentAvArbeidsgiver
        val tidBrukt = Duration.between(inntektsgrunnlagInnhentet, godkjentAvArbeidsgiver)

        timer.record(tidBrukt)
    }

}
