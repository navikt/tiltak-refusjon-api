package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import java.time.LocalDate
import java.util.TreeMap

data class Beregningskontekst(
    val grunnbelop: TreeMap<LocalDate, Int>
)
