package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import java.time.LocalDateTime

data class RefusjonEndretStatusMelding(
    val refusjonId: String,
    val bedriftNr: String,
    val avtaleId: String,
    val status: RefusjonStatus
)
