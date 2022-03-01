package no.nav.arbeidsgiver.tiltakrefusjon.utils

import java.time.LocalDate
import java.time.YearMonth

fun erMånedIPeriode(måned: YearMonth, fra: LocalDate, til: LocalDate): Boolean {
    return fra.datesUntil(til.plusDays(1)).anyMatch { dag -> YearMonth.of(dag.year, dag.month) == måned }
}

fun antallMånederEtter(dato: LocalDate, antall: Long): LocalDate {
    val datoEtterAntallMåneder = dato.plusMonths(antall)
    if (datoEtterAntallMåneder.dayOfMonth < dato.dayOfMonth) {
        return datoEtterAntallMåneder.plusDays(1)
    }
    return datoEtterAntallMåneder

    /**
     *     val datoEtterAntallMåneder = dato.plusMonths(antall)
           if(dato.lengthOfMonth() == dato.dayOfMonth && datoEtterAntallMåneder.lengthOfMonth() != datoEtterAntallMåneder.dayOfMonth) {
               return LocalDate.of(datoEtterAntallMåneder.year, datoEtterAntallMåneder.month, datoEtterAntallMåneder.lengthOfMonth())
           }
           return  datoEtterAntallMåneder;
     * */
}