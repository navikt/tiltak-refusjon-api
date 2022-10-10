package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

data class RefusjonEndretStatusMelding(
    val refusjonId: String,
    val bedriftNr: String,
    val avtaleId: String,
    val status: RefusjonStatus,
    val tilskuddsperiodeId: String
)
