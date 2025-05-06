package no.nav.arbeidsgiver.tiltakrefusjon.utils

import java.time.LocalDate

val åretsG = 124028
val forrigeÅretsG = 118620
val datoForGJustering = LocalDate.of(2024, 5, 1)

// Returnerer det man får opp til 5G. Altså 5G - Totalt utbetalt
fun gjenståendeEtterMaks5G(sumUtbetalt: Int, tilskuddFom: LocalDate): Int {
    if (tilskuddFom.plusDays(1).isBefore(datoForGJustering)) {
        return 0.coerceAtLeast(5 * forrigeÅretsG - sumUtbetalt)
    }
    return 0.coerceAtLeast(5 * åretsG - sumUtbetalt)
}
