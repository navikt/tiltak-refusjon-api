package no.nav.arbeidsgiver.tiltakrefusjon.refusjon.events

import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.Refusjon
import java.time.LocalDate

data class FristForlenget(
    override val refusjon: Refusjon,
    val gammelFrist: LocalDate,
    val nyFrist: LocalDate,
    val årsak: String,
    override val utførtAv: String
) : SporbarRefusjonHendelse
