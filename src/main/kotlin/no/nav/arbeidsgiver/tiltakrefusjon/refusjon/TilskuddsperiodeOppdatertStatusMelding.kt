package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

data class TilskuddsperiodeOppdatertStatusMelding(
    val refusjonId: String,
    val tilskuddsperiodeId: String,
    val utførtAv: String,
    val status: RefusjonStatus,
    val grunn: String,
)
