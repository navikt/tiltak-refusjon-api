package no.nav.arbeidsgiver.tiltakrefusjon.refusjon.events

import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.Refusjon

data class KorreksjonMerketForTilbakekreving(override val refusjon: Refusjon, override val utførtAv: String) :
    SporbarRefusjonHendelse
