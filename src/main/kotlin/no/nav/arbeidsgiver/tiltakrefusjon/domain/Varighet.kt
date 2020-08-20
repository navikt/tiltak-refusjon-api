package no.nav.arbeidsgiver.tiltakrefusjon.domain

import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter
import java.util.*

val format = "d. MMMM yyyy"
val localeNo: Locale = Locale.forLanguageTag("no")
val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern(format, localeNo);

data class Varighet(val datoFra: LocalDate, val datoTil: LocalDate) {
    val periode: Period = beregn(datoFra, datoTil)
    val fraDato: String = format(datoFra)
    val tilDato: String = format(datoTil)
    val maaneder: Int = periode.months
    var dager: Int = periode.days
}


fun beregn(fraDato: LocalDate, tilDato: LocalDate): Period {
    return Period.between(fraDato, tilDato)
}

fun format(dato: LocalDate): String {
    return dato.format(formatter);
}
