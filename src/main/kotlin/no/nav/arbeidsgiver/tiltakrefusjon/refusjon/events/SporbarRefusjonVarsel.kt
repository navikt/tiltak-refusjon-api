package no.nav.arbeidsgiver.tiltakrefusjon.refusjon.events

interface SporbarRefusjonVarsel {
    val refusjonId: String
    val utførtAv: String
}