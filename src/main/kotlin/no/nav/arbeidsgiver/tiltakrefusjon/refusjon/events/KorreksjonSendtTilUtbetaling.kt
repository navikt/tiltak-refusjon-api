package no.nav.arbeidsgiver.tiltakrefusjon.refusjon.events

import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.Korreksjonstype
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.Refusjon

data class KorreksjonSendtTilUtbetaling(val refusjon: Refusjon, val korreksjonstype: Korreksjonstype)
