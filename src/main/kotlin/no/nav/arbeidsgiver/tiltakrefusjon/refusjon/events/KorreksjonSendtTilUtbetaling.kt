package no.nav.arbeidsgiver.tiltakrefusjon.refusjon.events

import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.Korreksjon
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.Korreksjonstype
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.Refusjon

data class KorreksjonSendtTilUtbetaling(
    override val korreksjon: Korreksjon,
    override val utførtAv: String
) : SporbarKorreksjonHendelse
