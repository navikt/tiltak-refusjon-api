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
        val sumGrunnlag =  inntekter.filter {
            it.inntektType == "LØNNSINNTEKT"
                    && it.beløp > 0.0
                    && innenPeriode(it.opptjeningsperiodeFom, it.opptjeningsperiodeFom)
        }.map {
            val ferie = it.beløp * feriepengersats!!
            val pensjon = (it.beløp + ferie) * tjenestepensjon!!
            val agAvgift = Math.round((it.beløp + pensjon + ferie) * arbeidsgiveravgift!!)

            (it.beløp + pensjon + ferie + agAvgift)
                    .div(it.opptjenteDager())
                    .times(stillingsprosent / 100.0)
        }
                .sum()
        return  Math.round(sumGrunnlag).toDouble()
    }

    private fun innenPeriode(opptjeningsperiodeFom: LocalDate?, opptjeningsperiodeTom: LocalDate?): Boolean {
        if ((opptjeningsperiodeFom == null || opptjeningsperiodeTom == null) || (startDato == null || sluttDato == null)) {
            return true
        }
        return (opptjeningsperiodeFom.isEqual(startDato) || opptjeningsperiodeFom.isAfter(startDato)) && (opptjeningsperiodeTom.isEqual(sluttDato) || opptjeningsperiodeTom.isBefore(sluttDato))
    }
}
