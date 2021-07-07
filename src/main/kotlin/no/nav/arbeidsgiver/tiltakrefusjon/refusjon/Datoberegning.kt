package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import java.time.LocalDate
import java.time.YearMonth

fun erMånedIPeriode(måned: YearMonth, fra: LocalDate, til: LocalDate): Boolean {
    return fra.datesUntil(til.plusDays(1)).anyMatch { dag -> YearMonth.of(dag.year, dag.month) == måned }
}