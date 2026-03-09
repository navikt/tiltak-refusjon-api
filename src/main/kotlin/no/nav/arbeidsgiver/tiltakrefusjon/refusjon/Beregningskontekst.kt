package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import no.nav.arbeidsgiver.tiltakrefusjon.grunnbelop.GrunnbelopService
import java.time.LocalDate
import java.util.TreeMap

data class Beregningskontekst(
    val alleGrunnbelop: TreeMap<LocalDate, Int>
) {
    constructor(grunnbelopService: GrunnbelopService): this(grunnbelopService.alleGrunnbelop())
}
