package no.nav.arbeidsgiver.tiltakrefusjon

import java.math.BigDecimal
import java.time.LocalDate

data class Inntektslinje(
        val inntektType: String,
        val beløp: BigDecimal,
        val opptjeningsperiodeFom: LocalDate,
        val opptjeningsperiodeTom: LocalDate
)
