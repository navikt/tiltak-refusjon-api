package no.nav.arbeidsgiver.tiltakrefusjon.refusjon.events

import no.nav.arbeidsgiver.tiltakrefusjon.varsling.VarselType

data class SendtVarsel(override val refusjonId: String, override val varselType: VarselType, override val utførtAv: String = "System") : SporbarRefusjonVarsel