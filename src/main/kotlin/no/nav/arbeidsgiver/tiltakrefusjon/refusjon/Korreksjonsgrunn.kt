package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

enum class Korreksjonsgrunn {
    HENT_INNTEKTER_PÅ_NYTT,
    UTBETALT_HELE_TILSKUDDSBELØP,
    INNTEKTER_RAPPORTERT_ETTER_TILSKUDDSPERIODE,
    UTBETALING_RETURNERT
}