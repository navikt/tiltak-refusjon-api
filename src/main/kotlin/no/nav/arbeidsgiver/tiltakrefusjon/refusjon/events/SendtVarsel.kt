package no.nav.arbeidsgiver.tiltakrefusjon.refusjon.events

data class SendtVarsel(override val refusjonId: String, override val utførtAv: String = "System") : SporbarRefusjonVarsel