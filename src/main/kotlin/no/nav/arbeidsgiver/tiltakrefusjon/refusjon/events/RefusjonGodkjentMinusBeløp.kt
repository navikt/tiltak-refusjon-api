package no.nav.arbeidsgiver.tiltakrefusjon.refusjon.events

import no.nav.arbeidsgiver.tiltakrefusjon.autorisering.InnloggetBruker
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.Refusjon

data class RefusjonGodkjentMinusBeløp(override val refusjon: Refusjon, override val utførtAv: InnloggetBruker) :
    SporbarRefusjonHendelse
