package no.nav.arbeidsgiver.tiltakrefusjon.refusjon.events

import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.Refusjon

interface SporbarRefusjonHendelse {
    val refusjon: Refusjon
    val utførtAv: String
}