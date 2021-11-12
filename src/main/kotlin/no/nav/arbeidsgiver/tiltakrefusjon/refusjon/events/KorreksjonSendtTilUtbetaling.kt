package no.nav.arbeidsgiver.tiltakrefusjon.refusjon.events

import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.Korreksjonstype
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.Refusjon

data class KorreksjonSendtTilUtbetaling(
    override val refusjon: Refusjon,
    override val utførtAv: String,
    val korreksjonstype: Korreksjonstype
) : SporbarRefusjonHendelse
