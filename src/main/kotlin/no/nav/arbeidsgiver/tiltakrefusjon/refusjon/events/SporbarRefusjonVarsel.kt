package no.nav.arbeidsgiver.tiltakrefusjon.refusjon.events
import no.nav.arbeidsgiver.tiltakrefusjon.varsling.VarselType

interface SporbarRefusjonVarsel {
    val refusjonId: String
    val utførtAv: String
    val varselType: VarselType
}