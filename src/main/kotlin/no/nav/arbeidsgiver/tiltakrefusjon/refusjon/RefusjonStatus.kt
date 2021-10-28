package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

enum class RefusjonStatus {
    KLAR_FOR_INNSENDING,
    FOR_TIDLIG,
    SENDT_KRAV,
    UTBETALT,
    UTGÅTT,
    ANNULLERT,
    KORREKSJON_UTKAST,
    KORREKSJON_SENDT_TIL_UTBETALING,
    KORREKSJON_OPPGJORT,
    KORREKSJON_SKAL_TILBAKEKREVES
}