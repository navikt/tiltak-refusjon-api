package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

enum class Korreksjonsgrunn {
    HENT_INNTEKTER_PÅ_NYTT,
    HENT_INNTEKTER_TO_MÅNEDER_FREM,
    TRUKKET_FEIL_FOR_FRAVÆR,
    OPPDATERTAMELDING,
    ANNEN_GRUNN,
    UTBETALT_HELE_TILSKUDDSBELØP,
    UTBETALING_RETURNERT
}