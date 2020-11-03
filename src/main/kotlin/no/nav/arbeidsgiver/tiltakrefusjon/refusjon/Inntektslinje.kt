package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

data class Inntektslinje(
        val inntektType: String,
        val beløp: Double,
        val måned: YearMonth,
        var opptjeningsperiodeFom: LocalDate?,
        var opptjeningsperiodeTom: LocalDate?
) {
    init {
        opptjeningsperiodeFom?.let {måned.atDay(1)}
        opptjeningsperiodeTom?.let {måned.atEndOfMonth()}
    }

    fun opptjenteDager(): Int {
        return opptjeningsperiodeFom!!.datesUntil(opptjeningsperiodeTom!!.plusDays(1))
                .filter { erHverdag(it) }
                .count()
                .toInt()
    }

    private fun erHverdag(dato: LocalDate): Boolean {
        return dato.dayOfWeek != DayOfWeek.SATURDAY && dato.dayOfWeek != DayOfWeek.SUNDAY
    }

    fun erLønnsinntekt() = inntektType == "LØNNSINNTEKT" && beløp > 0.0
    fun innenPeriode(startDato: LocalDate?, sluttDato: LocalDate?): Boolean {
        if ((opptjeningsperiodeFom == null || opptjeningsperiodeTom == null) || (startDato == null || sluttDato == null)) {
            return true
        }
        return (opptjeningsperiodeFom!!.isEqual(startDato) || opptjeningsperiodeFom!!.isAfter(startDato))
                && (opptjeningsperiodeTom!!.isEqual(sluttDato) || opptjeningsperiodeTom!!.isBefore(sluttDato))
    }
}
