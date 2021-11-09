package no.nav.arbeidsgiver.tiltakrefusjon.refusjon.events

import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.Korreksjon

data class KorreksjonMerketForTilbakekreving(override val korreksjon: Korreksjon, override val utførtAv: String) :
    SporbarKorreksjonHendelse
