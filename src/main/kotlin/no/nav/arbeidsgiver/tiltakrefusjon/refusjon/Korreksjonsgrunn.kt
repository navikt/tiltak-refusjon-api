package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

enum class Korreksjonsgrunn {
    HENT_INNTEKTER_PÅ_NYTT,
    HENT_INNTEKTER_TO_MÅNEDER_FREM,
    UTBETALT_HELE_TILSKUDDSBELØP,
    UTBETALING_RETURNERT
}