package no.nav.arbeidsgiver.tiltakrefusjon.refusjon.events

import no.nav.arbeidsgiver.tiltakrefusjon.autorisering.InnloggetBruker
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.Refusjon
import java.time.LocalDate

data class KryssetAvForFravær(
    override val refusjon: Refusjon,
    val gammelFrist: LocalDate,
    val nyFrist: LocalDate,
    val årsak: String,
    override val utførtAv: InnloggetBruker
) : SporbarRefusjonHendelse
