package no.nav.arbeidsgiver.tiltakrefusjon

import java.time.LocalDate

data class Refusjonsgrunnlag(
        val inntekter: List<Inntektslinje>,
        val prosent: Int,
        val startDato: LocalDate?,
        val sluttDato: LocalDate?
)
