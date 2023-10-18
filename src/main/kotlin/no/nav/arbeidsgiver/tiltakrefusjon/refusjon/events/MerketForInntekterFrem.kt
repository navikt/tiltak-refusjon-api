package no.nav.arbeidsgiver.tiltakrefusjon.refusjon.events

import no.nav.arbeidsgiver.tiltakrefusjon.autorisering.InnloggetBruker
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.BrukerRolle
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.Refusjon

data class MerketForInntekterFrem(override val refusjon: Refusjon, val merking: Boolean, override val utførtAv: InnloggetBruker
) : SporbarRefusjonHendelse
