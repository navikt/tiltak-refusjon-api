package no.nav.arbeidsgiver.tiltakrefusjon.autorisering

import java.util.*

data class InternIdentifikatorer(
    val navIdent: String,
    val azureOid: UUID
)
