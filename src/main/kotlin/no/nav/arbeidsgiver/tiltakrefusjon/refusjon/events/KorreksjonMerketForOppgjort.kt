package no.nav.arbeidsgiver.tiltakrefusjon.refusjon.events

import no.nav.arbeidsgiver.tiltakrefusjon.autorisering.InnloggetBruker
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.Korreksjon

data class KorreksjonMerketForOppgjort(override val korreksjon: Korreksjon, override val utførtAv: InnloggetBruker
) :
    SporbarKorreksjonHendelse
