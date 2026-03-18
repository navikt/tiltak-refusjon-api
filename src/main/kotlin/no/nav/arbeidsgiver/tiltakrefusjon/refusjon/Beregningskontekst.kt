package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import no.nav.arbeidsgiver.tiltakrefusjon.grunnbelop.Grunnbelop
import java.time.LocalDate
import java.util.*

class Beregningskontekst(
    private val alleGrunnbelop: TreeMap<LocalDate, Int>
) {
    /**
     * Hent grunnbeløpet som gjelder for en gitt dato.
     * Vil kaste feilmelding dersom vi ikke finner beløp for datoen; det vil
     * typisk skje dersom man spør om en veldig gammel dato, feks før 1970.
     */
    fun grunnbelopForPerioden(tilskuddFom: LocalDate): Grunnbelop =
        alleGrunnbelop.floorEntry(tilskuddFom)?.let {
            Grunnbelop(it.key, it.value)
        } ?: throw RuntimeException("Fant ikke grunnbeløp for dato $tilskuddFom.")
}
