package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import no.nav.arbeidsgiver.tiltakrefusjon.utils.Now
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.YearMonth

internal class InntektslinjeTest() {

    @Test
    fun `inntektslinje uten beskrivelse er ikke med i beregningsgrunnlag`() {
        val inntektslinje = Inntektslinje(
            inntektType = "LOENNSINNTEKT",
            beskrivelse = null,
            beløp = 25000.0,
            måned = YearMonth.now(),
            opptjeningsperiodeFom = Now.localDate(),
            opptjeningsperiodeTom = Now.localDate().plusDays(30)
        )
        assertThat(inntektslinje.erMedIInntektsgrunnlag()).isFalse()
    }
}