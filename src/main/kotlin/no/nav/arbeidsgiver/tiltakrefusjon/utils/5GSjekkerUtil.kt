package no.nav.arbeidsgiver.tiltakrefusjon.utils

import java.time.LocalDate

val åretsG = 118620
val forrigeÅretsG = 111477
val datoForGJustering = LocalDate.of(2023, 5, 1)

fun erOver5G(totaltUtbetalt: Int): Boolean {

    return false
}

// Returnerer det man får opp til 5G. Altså 5G - Totalt utbetalt
fun capEtterMaks5G(sumUtbetalt: Double, tilskuddFom: LocalDate): Double {
    if(tilskuddFom.plusDays(1).isBefore(datoForGJustering)) {
        return 0.0.coerceAtLeast(5 * forrigeÅretsG - sumUtbetalt)
    }
    return 0.0.coerceAtLeast(5 * åretsG - sumUtbetalt)
}