package no.nav.arbeidsgiver.tiltakrefusjon.refusjon.events

import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.Korreksjon
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.Refusjon

data class KorreksjonSendtTilUtbetaling(val refusjon: Refusjon, val korreksjon: Korreksjon)
