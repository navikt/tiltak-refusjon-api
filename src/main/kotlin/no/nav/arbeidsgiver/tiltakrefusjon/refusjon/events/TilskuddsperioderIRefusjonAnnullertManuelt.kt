package no.nav.arbeidsgiver.tiltakrefusjon.refusjon.events

import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.Refusjon

data class TilskuddsperioderIRefusjonAnnullertManuelt(override val refusjon: Refusjon, override val utførtAv: String, val annulleringsgrunn: String) :
    SporbarRefusjonHendelse
