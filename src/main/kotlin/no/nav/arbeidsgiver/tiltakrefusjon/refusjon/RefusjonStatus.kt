package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

enum class RefusjonStatus {
    KLAR_FOR_INNSENDING,
    FOR_TIDLIG,
    SENDT_KRAV,
    UTBETALT,
    KORRIGERT,
    UTGÅTT,
    ANNULLERT,
    GODKJENT_MINUSBELØP,
    UTBETALING_FEILET
}