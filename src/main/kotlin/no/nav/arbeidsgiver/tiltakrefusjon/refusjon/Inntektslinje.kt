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
    fun opptjenteDager(): Int {
        setOpptjentingsperiode()
        return opptjeningsperiodeFom!!.datesUntil(opptjeningsperiodeTom!!.plusDays(1))
                .filter { erHverdag(it) }
                .count()
                .toInt()
    }

    private fun setOpptjentingsperiode() {
        if (opptjeningsperiodeFom == null) {
            opptjeningsperiodeFom = måned.atDay(1)
        }
        if (opptjeningsperiodeTom == null) {
            opptjeningsperiodeTom = måned.atEndOfMonth()
        }
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
