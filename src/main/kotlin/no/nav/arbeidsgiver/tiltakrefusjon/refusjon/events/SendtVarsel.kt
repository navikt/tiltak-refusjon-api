package no.nav.arbeidsgiver.tiltakrefusjon.refusjon.events

import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.BrukerRolle
import no.nav.arbeidsgiver.tiltakrefusjon.varsling.VarselType

data class SendtVarsel(override val refusjonId: String, override val varselType: VarselType, override val utførtAv: String = "System",
                       override val utførtRolle: BrukerRolle = BrukerRolle.SYSTEM
) : SporbarRefusjonVarsel