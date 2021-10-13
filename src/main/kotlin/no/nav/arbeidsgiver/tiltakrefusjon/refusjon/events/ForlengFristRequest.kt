package no.nav.arbeidsgiver.tiltakrefusjon.refusjon.events

import java.time.LocalDate

data class ForlengFristRequest(
    val nyFrist: LocalDate,
    val årsak: String
)