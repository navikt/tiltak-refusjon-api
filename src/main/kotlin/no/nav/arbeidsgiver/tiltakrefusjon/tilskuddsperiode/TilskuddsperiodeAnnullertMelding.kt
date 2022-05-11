package no.nav.arbeidsgiver.tiltakrefusjon.tilskuddsperiode

data class TilskuddsperiodeAnnullertMelding(
    val tilskuddsperiodeId: String,
    val årsak: MidlerFrigjortÅrsak,
)

enum class MidlerFrigjortÅrsak {
    AVTALE_ANNULLERT,
    REFUSJON_FRIST_UTGÅTT,
    REFUSJON_IKKE_SØKT,
}