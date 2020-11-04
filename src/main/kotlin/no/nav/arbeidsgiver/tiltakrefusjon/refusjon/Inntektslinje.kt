package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

data class Inntektslinje(
        val inntektType: String = "LØNNSINNTEKT",
        val beløp: Double = 0.0,
        val måned: YearMonth = YearMonth.now(),
        val inntektsperiodeFom: LocalDate = måned.atDay(1),
        val inntektsperiodeTom: LocalDate = måned.atEndOfMonth()
) {

    constructor(inntektType:String, beløp: Double?, måned: YearMonth, inntektsperiodeFom: LocalDate? = null, inntektsperiodeTom: LocalDate? = null):
            this(inntektType,beløp ?: 0.0 ,måned, inntektsperiodeFom ?: måned.atDay(1), inntektsperiodeTom ?: måned.atEndOfMonth())

    fun hentAntallOpptjenteDager(fraDato: LocalDate, tilDato: LocalDate): Int {
        return inntektsperiodeFom.datesUntil(inntektsperiodeTom.plusDays(1))
                .filter(this::erHverdag)
                .filter{ !it.isBefore(fraDato) && !it.isAfter(tilDato) }
                .count()
                .toInt()
    }

    private fun erHverdag(dato: LocalDate): Boolean {
        return dato.dayOfWeek != DayOfWeek.SATURDAY && dato.dayOfWeek != DayOfWeek.SUNDAY
    }

    fun erLønnsinntekt() = inntektType == "LØNNSINNTEKT" && beløp > 0.0
}
