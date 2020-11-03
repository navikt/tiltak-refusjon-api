package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import java.time.LocalDate

data class Refusjonsgrunnlag(
        val inntekter: List<Inntektslinje>,
        val stillingsprosent: Int,
        val startDato: LocalDate?,
        val sluttDato: LocalDate?,
        var arbeidsgiveravgift:Double?,
        var feriepengersats:Double?,
        var tjenestepensjon:Double?
) {
    fun hentBeregnetGrunnlag(): Double {
        return inntekter
                .filter {inntekt ->
                    erLønnsinntekt(inntekt) &&
                    innenPeriode(inntekt.opptjeningsperiodeFom, inntekt.opptjeningsperiodeFom)
        }.map {inntekt ->
            val totalFeriepenger = inntekt.beløp * feriepengersats!!
            val totalTjenestepensjon = (inntekt.beløp + totalFeriepenger) * tjenestepensjon!!
            val totalArbeidsgiveravgift = Math.round((inntekt.beløp + totalTjenestepensjon + totalFeriepenger) * arbeidsgiveravgift!!)
            val total = inntekt.beløp + totalTjenestepensjon + totalFeriepenger + totalArbeidsgiveravgift
            total.div(inntekt.opptjenteDager()).times(stillingsprosent / 100.0)
        }.sum()
    }

    private fun erLønnsinntekt(inntekt: Inntektslinje) = inntekt.inntektType == "LØNNSINNTEKT" && inntekt.beløp > 0.0

    private fun innenPeriode(opptjeningsperiodeFom: LocalDate?, opptjeningsperiodeTom: LocalDate?): Boolean {
        if ((opptjeningsperiodeFom == null || opptjeningsperiodeTom == null) || (startDato == null || sluttDato == null)) {
            return true
        }
        return (opptjeningsperiodeFom.isEqual(startDato) || opptjeningsperiodeFom.isAfter(startDato)) && (opptjeningsperiodeTom.isEqual(sluttDato) || opptjeningsperiodeTom.isBefore(sluttDato))
    }
}
