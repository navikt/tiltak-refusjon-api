package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import java.time.LocalDate
import kotlin.math.roundToInt

data class Refusjonsgrunnlag(
        val inntekter: List<Inntektslinje>,
        val stillingsprosent: Int,
        val datoRefusjonstart: LocalDate,
        val datoRefusjonslutt: LocalDate,
        var arbeidsgiveravgiftSats: Double?,
        var feriepengerSats: Double?,
        var tjenestepensjonSats: Double?
) {
    fun hentBeregnetGrunnlag(): Int {
        return inntekter
                .filter(Inntektslinje::erLønnsinntekt)
                .map { inntekt ->
                    val antallDagerIPeriode = inntekt.hentAntallOpptjenteDagerInnenPeriode(datoRefusjonstart, datoRefusjonslutt)
                    if( antallDagerIPeriode == 0 ) return 0
                    val feriepenger = inntekt.beløp * feriepengerSats!!
                    val tjenestepensjon = (inntekt.beløp + feriepenger) * tjenestepensjonSats!!
                    val arbeidsgiveravgift = (inntekt.beløp + tjenestepensjon + feriepenger) * arbeidsgiveravgiftSats!!
                    val total = inntekt.beløp + tjenestepensjon + feriepenger + arbeidsgiveravgift
                    total.div(antallDagerIPeriode)
                }
                .sum()
                .times(stillingsprosent / 100.0)
                .roundToInt()
    }
}
