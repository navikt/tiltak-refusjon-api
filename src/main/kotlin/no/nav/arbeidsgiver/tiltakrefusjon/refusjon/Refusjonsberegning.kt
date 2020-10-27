package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import java.math.BigDecimal

fun beregnRefusjon(grunnlag: Refusjonsgrunnlag): BigDecimal {
    val inntekt = grunnlag.inntekter
            .map { it.beløp }
            .fold(BigDecimal.ZERO, BigDecimal::add)

    val prosentBigDecimal = (grunnlag.prosent / 100.0).toBigDecimal()
    return inntekt * prosentBigDecimal
}