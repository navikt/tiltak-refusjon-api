package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

data class Inntektslinje(
        val inntektType: String,
        val beløp: Double,
        val måned: YearMonth,
        val opptjeningsperiodeFom: LocalDate = måned.atDay(1),
        val opptjeningsperiodeTom: LocalDate = måned.atEndOfMonth()
){

    fun opptjenteDager():Int{
        return opptjeningsperiodeFom.datesUntil(opptjeningsperiodeTom.plusDays(1))
                .filter{erHverdag(it)}
                .count()
                .toInt()
    }

    private fun erHverdag(dato: LocalDate): Boolean {
        return dato.dayOfWeek != DayOfWeek.SATURDAY && dato.dayOfWeek != DayOfWeek.SUNDAY
    }

}
