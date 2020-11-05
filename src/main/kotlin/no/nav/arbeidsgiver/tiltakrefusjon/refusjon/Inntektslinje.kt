package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import kotlin.streams.toList

data class Inntektslinje(
        val inntektType: String = "LØNNSINNTEKT",
        val beløp: Double = 0.0,
        val måned: YearMonth = YearMonth.now(),
        val opptjeningsperiodeFom: LocalDate = måned.atDay(1),
        val opptjenningsperiodeTom: LocalDate = måned.atEndOfMonth()
) {

    constructor(inntektType:String, beløp: Double?, måned: YearMonth, inntektsperiodeFom: LocalDate? = null, inntektsperiodeTom: LocalDate? = null):
            this(inntektType,beløp ?: 0.0 ,måned, inntektsperiodeFom ?: måned.atDay(1), inntektsperiodeTom ?: måned.atEndOfMonth())


    fun hentAntallDagerOpptjent():Int{
        return dagerOpptjentList()
                .count()
    }

    fun hentAntallOpptjenteDagerInnenPeriode(fraDato: LocalDate, tilDato: LocalDate): Int {
        return dagerOpptjentList()
                .filter{ !it.isBefore(fraDato) && !it.isAfter(tilDato) }
                .count()
    }

    private fun dagerOpptjentList() = opptjeningsperiodeFom.datesUntil(opptjenningsperiodeTom.plusDays(1)).filter(this::erHverdag).toList()

    private fun erHverdag(dato: LocalDate): Boolean {
        return dato.dayOfWeek != DayOfWeek.SATURDAY && dato.dayOfWeek != DayOfWeek.SUNDAY
    }

    fun erLønnsinntekt() = inntektType == "LØNNSINNTEKT" && beløp > 0.0
    fun hentBeløpPerDag(): Double {
        return beløp / hentAntallDagerOpptjent()
    }
}
