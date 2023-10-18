package no.nav.arbeidsgiver.tiltakrefusjon.refusjon.events

import no.nav.arbeidsgiver.tiltakrefusjon.autorisering.InnloggetBruker
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.BrukerRolle
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.Refusjon

data class SaksbehandlerMerketForInntekterLengerFrem(override val refusjon: Refusjon, val merking: Int, override val utførtAv: InnloggetBruker,
) : SporbarRefusjonHendelse
