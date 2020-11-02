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
){

    fun opptjenteDager():Int{
        if(opptjeningsperiodeFom  == null ){
            opptjeningsperiodeFom = måned.atDay(1)
        }
        if(opptjeningsperiodeTom  == null ){
            opptjeningsperiodeTom = måned.atEndOfMonth()
        }

        return opptjeningsperiodeFom?.datesUntil(opptjeningsperiodeTom?.plusDays(1))?.filter{
            dato -> erHverdag(dato)
        }?.count()?.toInt()!!
    }

    private fun erHverdag(dato: LocalDate): Boolean {
        return dato.dayOfWeek != DayOfWeek.SATURDAY && dato.dayOfWeek != DayOfWeek.SUNDAY
    }

}
