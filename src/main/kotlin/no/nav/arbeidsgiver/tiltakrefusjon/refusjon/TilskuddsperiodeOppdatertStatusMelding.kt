package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

data class TilskuddsperiodeOppdatertStatusMelding(
    val status: RefusjonStatus,
    val tilskuddsperiodeId: String,
    val refusjonId: String,
    val avtaleId: String,
    val grunn: String,
)
