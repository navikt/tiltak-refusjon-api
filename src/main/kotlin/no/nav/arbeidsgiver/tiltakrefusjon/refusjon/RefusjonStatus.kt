package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

enum class RefusjonStatus {
    KLAR_FOR_INNSENDING,
    FOR_TIDLIG,
    SENDT_KRAV,
    UTBETALT,
    KORRIGERT,
    UTGÅTT,
    ANNULLERT,
    UTBETALING_FEILET
}