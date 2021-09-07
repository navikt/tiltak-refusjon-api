package no.nav.arbeidsgiver.tiltakrefusjon

open class FeilkodeException(val feilkode: Feilkode) : RuntimeException("Feil inntruffet: $feilkode")

enum class Feilkode {
    TEKNISK_FEIL_BANKKONTONUMMEROPPSLAG,
    UGYLDIG_STATUS,
    ETTER_FRIST,
    INGEN_INNTEKTER,
    INGEN_BEDRIFTKONTONUMMER,
    ALTINN,
    HAR_KORREKSJON,
    INGEN_KORREKSJONSGRUNNER,
    INNTEKTER_KUN_FRA_TILTAK_OG_OPPGIR_BELØP,
    SAKSBEHANDLER_SVARER_PÅ_INNTEKTSPØRSMÅL
}
