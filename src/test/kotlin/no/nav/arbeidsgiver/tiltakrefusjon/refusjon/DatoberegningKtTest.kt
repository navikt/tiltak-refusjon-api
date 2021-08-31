package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import no.nav.arbeidsgiver.tiltakrefusjon.utils.antallMånederEtter
import no.nav.arbeidsgiver.tiltakrefusjon.utils.erMånedIPeriode
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

    @Test
    internal fun `to måneder etter i måned med 31 dager`() {
        assertThat(antallMånederEtter(LocalDate.of(2021, 7, 31), 2)).isEqualTo(LocalDate.of(2021, 10, 1))
    }

    @Test
    internal fun `to måneder til vanlig`() {
        assertThat(antallMånederEtter(LocalDate.of(2021, 7, 30), 2)).isEqualTo(LocalDate.of(2021, 9, 30))
    }

    @Test
    internal fun `to måneder fra desember til februar`() {
        assertThat(antallMånederEtter(LocalDate.of(2021, 12, 31), 2)).isEqualTo(LocalDate.of(2022, 3, 1))
    }
}