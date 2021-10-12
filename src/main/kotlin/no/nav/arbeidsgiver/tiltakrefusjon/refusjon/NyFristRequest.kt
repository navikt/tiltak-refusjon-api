package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import java.time.LocalDate

data class NyFristRequest(
    val nyFrist: LocalDate,
    val årsak: String
)