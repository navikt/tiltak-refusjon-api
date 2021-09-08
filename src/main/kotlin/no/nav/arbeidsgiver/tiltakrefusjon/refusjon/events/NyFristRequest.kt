package no.nav.arbeidsgiver.tiltakrefusjon.refusjon.events

import java.time.LocalDate

data class NyFristRequest(
    val nyFrist: LocalDate
    )