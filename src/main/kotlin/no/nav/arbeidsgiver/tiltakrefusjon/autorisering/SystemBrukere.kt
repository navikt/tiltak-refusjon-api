package no.nav.arbeidsgiver.tiltakrefusjon.autorisering

import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.BrukerRolle

val KAFKA_BRUKER = object : InnloggetBruker {
    override val identifikator: String
        get() = "kafka"
    override val rolle: BrukerRolle
        get() = BrukerRolle.SYSTEM
}

val ADMIN_BRUKER = object : InnloggetBruker {
    override val identifikator: String
        get() = "admin"
    override val rolle: BrukerRolle
        get() = BrukerRolle.SYSTEM
}

val SYSTEM_BRUKER = object : InnloggetBruker {
    override val identifikator: String
        get() = "system"
    override val rolle: BrukerRolle
        get() = BrukerRolle.SYSTEM
}