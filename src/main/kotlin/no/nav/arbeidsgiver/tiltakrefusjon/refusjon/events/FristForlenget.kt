package no.nav.arbeidsgiver.tiltakrefusjon.refusjon.events

import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.Refusjon
import java.time.LocalDate

data class FristForlenget(
    val refusjon: Refusjon,
    val gammelFrist: LocalDate,
    val nyFrist: LocalDate,
    val årsak: String,
    val utførtAv: String
)
