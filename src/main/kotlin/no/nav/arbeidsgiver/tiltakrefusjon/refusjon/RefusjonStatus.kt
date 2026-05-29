package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

enum class RefusjonStatus : RefunderingStatus {
    KLAR_FOR_INNSENDING,
    FOR_TIDLIG,
    SENDT_KRAV,
    UTBETALT,
    KORRIGERT,
    UTGÅTT,
    ANNULLERT,
    GODKJENT_MINUSBELØP,
    UTBETALING_FEILET,
    GODKJENT_NULLBELØP;

    override fun isSendtInn() = when (this) {
        KLAR_FOR_INNSENDING, FOR_TIDLIG -> false
        SENDT_KRAV, UTBETALT, KORRIGERT, UTGÅTT, ANNULLERT, GODKJENT_MINUSBELØP, GODKJENT_NULLBELØP, UTBETALING_FEILET -> true
    }

    fun ansesSomUtbetalt() = when (this) {
        KLAR_FOR_INNSENDING, FOR_TIDLIG, ANNULLERT, GODKJENT_MINUSBELØP, KORRIGERT, UTGÅTT -> false
        SENDT_KRAV, UTBETALT, GODKJENT_NULLBELØP -> true
        // Feilede utbetalinger kan bli rettet opp i Oebs og må derfor anses som utbetalt
        UTBETALING_FEILET -> true
    }
}
