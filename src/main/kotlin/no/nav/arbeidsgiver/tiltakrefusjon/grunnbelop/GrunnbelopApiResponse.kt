package no.nav.arbeidsgiver.tiltakrefusjon.grunnbelop

import java.time.LocalDate

data class GrunnbelopApiResponse (
    val dato: LocalDate,
    val grunnbeløp: Int
)
