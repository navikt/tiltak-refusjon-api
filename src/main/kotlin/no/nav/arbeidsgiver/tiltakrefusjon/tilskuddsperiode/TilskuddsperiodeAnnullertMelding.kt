package no.nav.arbeidsgiver.tiltakrefusjon.tilskuddsperiode

data class TilskuddsperiodeAnnullertMelding(
    val tilskuddsperiodeId: String,
    val årsak: TilskuddsperiodeAnnullertÅrsak,
)

enum class TilskuddsperiodeAnnullertÅrsak {
    AVTALE_ANNULLERT,
    REFUSJON_FRIST_UTGÅTT,
    REFUSJON_IKKE_SØKT,
}