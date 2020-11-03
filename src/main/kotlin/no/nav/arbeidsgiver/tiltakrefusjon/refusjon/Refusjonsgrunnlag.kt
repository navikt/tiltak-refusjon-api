package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import java.time.LocalDate
import kotlin.math.roundToInt

data class Refusjonsgrunnlag(
        val inntekter: List<Inntektslinje>,
        val stillingsprosent: Int,
        val startDato: LocalDate?,
        val sluttDato: LocalDate?,
        var arbeidsgiveravgift: Double?,
        var feriepengersats: Double?,
        var tjenestepensjon: Double?
) {
    fun hentBeregnetGrunnlag(): Int {
        return inntekter
                .filter { it.erLønnsinntekt() && it.innenPeriode(startDato, sluttDato) }
                .map { inntekt ->
                    val totalFeriepenger = inntekt.beløp * feriepengersats!!
                    val totalTjenestepensjon = (inntekt.beløp + totalFeriepenger) * tjenestepensjon!!
                    val totalArbeidsgiveravgift = Math.round((inntekt.beløp + totalTjenestepensjon + totalFeriepenger) * arbeidsgiveravgift!!)
                    val total = Math.round(inntekt.beløp + totalTjenestepensjon + totalFeriepenger + totalArbeidsgiveravgift)
                    total.div(inntekt.opptjenteDager())
                            .times(stillingsprosent / 100.0)
                            .roundToInt()
                }.sum()
    }
}
