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
                .filter { it.antallOpptjenteDager(datoRefusjonstart, datoRefusjonslutt) > 0 }
                .map { inntekt ->
                    val feriepenger = inntekt.beløp * feriepengerSats!!
                    val tjenestepensjon = (inntekt.beløp + feriepenger) * tjenestepensjonSats!!
                    val arbeidsgiveravgift = (inntekt.beløp + tjenestepensjon + feriepenger) * arbeidsgiveravgiftSats!!
                    val total = inntekt.beløp + tjenestepensjon + feriepenger + arbeidsgiveravgift
                    total.div(inntekt.antallOpptjenteDager(datoRefusjonstart, datoRefusjonslutt))
                }
                .sum()
                .times(stillingsprosent / 100.0)
                .roundToInt()
    }
}
