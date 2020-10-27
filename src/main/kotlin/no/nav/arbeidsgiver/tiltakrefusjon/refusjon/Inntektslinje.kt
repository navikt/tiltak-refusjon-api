package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import java.math.BigDecimal
import java.time.LocalDate
import java.time.YearMonth

data class Inntektslinje(
        val inntektType: String,
        val beløp: BigDecimal,
        val måned: YearMonth,
        val opptjeningsperiodeFom: LocalDate?,
        val opptjeningsperiodeTom: LocalDate?
)
