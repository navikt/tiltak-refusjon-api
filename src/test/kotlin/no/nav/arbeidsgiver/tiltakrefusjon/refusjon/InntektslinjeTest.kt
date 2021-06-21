package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.YearMonth

internal class InntektslinjeTest() {

    @Test
    fun `inntektslinje uten beskrivelse er ikke med i beregningsgrunnlag`() {
        val inntektslinje = Inntektslinje(
            inntektType = "LOENNSINNTEKT",
            beskrivelse = null,
            beløp = 25000.0,
            måned = YearMonth.now(),
            opptjeningsperiodeFom = LocalDate.now(),
            opptjeningsperiodeTom = LocalDate.now().plusDays(30)
        )
        assertThat(inntektslinje.erMedIInntektsgrunnlag()).isFalse()
    }
}