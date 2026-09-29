package no.nav.arbeidsgiver.tiltakrefusjon.utregning

/**
 * Maksbeløpet som gjorde at en utregningslinje utgikk. Linjen vises fortsatt, men overstrøket, fordi
 * beløpet er erstattet av et lavere tak lenger ned i gruppa.
 */
enum class Maksbeløp {
    /** Erstattet av [UtregningsradType.AVTALT_BELOP]. */
    AVTALT_TILSKUDD,

    /** Erstattet av [UtregningsradType.AVTALT_BELOP_REST_5G]. */
    FEM_GRUNNBELOP
}
