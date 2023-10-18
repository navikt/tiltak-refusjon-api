package no.nav.arbeidsgiver.tiltakrefusjon.refusjon.events

import no.nav.arbeidsgiver.tiltakrefusjon.autorisering.InnloggetBruker
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.BrukerRolle
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.Refusjon

data class TilskuddsperioderIRefusjonAnnullertManuelt(override val refusjon: Refusjon, override val utførtAv: InnloggetBruker, val annulleringsgrunn: String,
) :
    SporbarRefusjonHendelse
