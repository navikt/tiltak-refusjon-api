package no.nav.arbeidsgiver.tiltakrefusjon.refusjon.events

import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.Refusjon

data class MerketForInntekterFrem(override val refusjon: Refusjon, val merking: Boolean, override val utførtAv: String) : SporbarRefusjonHendelse
