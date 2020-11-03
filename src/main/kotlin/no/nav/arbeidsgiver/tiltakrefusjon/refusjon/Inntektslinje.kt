package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

data class Inntektslinje(
        val inntektType: String,
        val beløp: Double,
        val måned: YearMonth,
        var inntektsperiodeFom: LocalDate?,
        var inntektsperiodeTom: LocalDate?
) {
    fun opptjenteDager(datoRefusjonslutt: LocalDate): Int {
        setOpptjentingsperiode()
        return inntektsperiodeFom!!.datesUntil(inntektsperiodeTom!!.plusDays(1))
                .filter { erHverdag(it) }
                .filter{ !it.isBefore(inntektsperiodeFom) && !it.isAfter(datoRefusjonslutt) }
                .count()
                .toInt()
    }

    private fun setOpptjentingsperiode() {
        if (inntektsperiodeFom == null) {
            inntektsperiodeFom = måned.atDay(1)
        }
        if (inntektsperiodeTom == null) {
            inntektsperiodeTom = måned.atEndOfMonth()
        }
    }

    private fun erHverdag(dato: LocalDate): Boolean {
        return dato.dayOfWeek != DayOfWeek.SATURDAY && dato.dayOfWeek != DayOfWeek.SUNDAY
    }

    fun erLønnsinntekt() = inntektType == "LØNNSINNTEKT" && beløp > 0.0

    fun innenPeriode(startDato: LocalDate?, sluttDato: LocalDate?): Boolean {
        if ((inntektsperiodeFom == null || inntektsperiodeTom == null) || (startDato == null || sluttDato == null)) {
            return true
        }
        return !inntektsperiodeFom!!.isBefore(startDato) && !inntektsperiodeTom!!.isAfter(sluttDato)
    }
}
