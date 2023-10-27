package no.nav.arbeidsgiver.tiltakrefusjon.refusjon.events

import no.nav.arbeidsgiver.tiltakrefusjon.autorisering.InnloggetBruker
import no.nav.arbeidsgiver.tiltakrefusjon.varsling.VarselType

interface SporbarRefusjonVarsel {
    val refusjonId: String
    val utførtAv: InnloggetBruker
    val varselType: VarselType
}