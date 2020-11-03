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
                .filter { it.erLønnsinntekt() && it.innenPeriode(datoRefusjonstart, datoRefusjonslutt) }
                .map { inntekt ->
                    val feriepenger = inntekt.beløp * feriepengerSats!!
                    val tjenestepensjon = (inntekt.beløp + feriepenger) * tjenestepensjonSats!!
                    val arbeidsgiveravgift = (inntekt.beløp + tjenestepensjon + feriepenger) * arbeidsgiveravgiftSats!!
                    val total = inntekt.beløp + tjenestepensjon + feriepenger + arbeidsgiveravgift
                    total.div(inntekt.opptjenteDager(datoRefusjonslutt))
                            .times(stillingsprosent / 100.0)
                            .roundToInt()
                }.sum()
    }
}
