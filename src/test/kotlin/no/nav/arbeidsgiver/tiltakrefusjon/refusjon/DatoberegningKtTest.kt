package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.YearMonth
import java.time.LocalDate

internal class DatoberegningKtTest {
    @Test
    internal fun `er i periode`() {
        assertThat(erMånedIPeriode(YearMonth.of(2021, 7), LocalDate.of(2021, 7, 1), LocalDate.of(2021, 7, 2))).isTrue()
        assertThat(erMånedIPeriode(YearMonth.of(2021, 6), LocalDate.of(2021, 7, 1), LocalDate.of(2021, 7, 2))).isFalse()
        assertThat(erMånedIPeriode(YearMonth.of(2021, 6), LocalDate.of(2021, 6, 1), LocalDate.of(2021, 7, 2))).isTrue()
        assertThat(erMånedIPeriode(YearMonth.of(2021, 6), LocalDate.of(2021, 8, 1), LocalDate.of(2021, 8, 2))).isFalse()
        assertThat(erMånedIPeriode(YearMonth.of(2020, 6), LocalDate.of(2021, 6, 1), LocalDate.of(2021, 7, 2))).isFalse()
    }
}