package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

enum class Korreksjonstype : RefunderingStatus {
    UTKAST, TILLEGSUTBETALING, OPPGJORT, TILBAKEKREVING, TILLEGGSUTBETALING_FEILET, TILLEGGSUTBETALING_UTBETALT;

    override fun isSendtInn() = when (this) {
        UTKAST -> false
        TILBAKEKREVING, OPPGJORT, TILLEGSUTBETALING, TILLEGGSUTBETALING_UTBETALT, TILLEGGSUTBETALING_FEILET -> true
    }
}
