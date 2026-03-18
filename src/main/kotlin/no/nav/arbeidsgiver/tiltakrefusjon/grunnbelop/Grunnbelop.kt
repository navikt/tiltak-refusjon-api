package no.nav.arbeidsgiver.tiltakrefusjon.grunnbelop

import java.time.LocalDate

data class Grunnbelop(
    val gjelderFraOgMed: LocalDate,
    val belop: Int,
)
