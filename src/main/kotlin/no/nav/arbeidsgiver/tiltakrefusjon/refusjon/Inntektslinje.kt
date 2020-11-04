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
    fun antallOpptjenteDager(datoRefusjonstart: LocalDate, datoRefusjonslutt: LocalDate): Int {
        setInntektsperiodeEnMånedOmIkkeSatt()
        return inntektsperiodeFom!!.datesUntil(inntektsperiodeTom!!.plusDays(1))
                .filter { erHverdag(it) }
                .filter{ !it.isBefore(datoRefusjonstart) && !it.isAfter(datoRefusjonslutt) }
                .count()
                .toInt()
    }

    private fun setInntektsperiodeEnMånedOmIkkeSatt() {
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
}
