package no.nav.arbeidsgiver.tiltakrefusjon.autorisering

import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.BrukerRolle

interface InnloggetBruker {
    val identifikator: String
    val rolle: BrukerRolle
}