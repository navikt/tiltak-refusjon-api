package no.nav.arbeidsgiver.tiltakrefusjon

open class FeilkodeException(val feilkode: Feilkode) : RuntimeException("Feil inntruffet: $feilkode")

enum class Feilkode {
    TEKNISK_FEIL_INNTEKTSOPPSLAG,
    TEKNISK_FEIL_BANKKONTONUMMEROPPSLAG,
    INNTEKT_HENTET_FOR_TIDLIG,
    UGYLDIG_STATUS,
    ETTER_FRIST,
    INGEN_INNTEKTER,
}
