package no.nav.arbeidsgiver.tiltakrefusjon.refusjon.events

import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.Korreksjon

interface SporbarKorreksjonHendelse {
    val korreksjon: Korreksjon
    val utførtAv: String
}