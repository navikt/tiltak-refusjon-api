package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import java.time.LocalDate
import java.util.TreeMap

data class Beregningskontekst(
    val alleGrunnbelop: TreeMap<LocalDate, Int>
)
