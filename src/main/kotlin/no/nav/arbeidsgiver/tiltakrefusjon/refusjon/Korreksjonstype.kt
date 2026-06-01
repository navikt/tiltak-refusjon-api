package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

enum class Korreksjonstype : RefunderingStatus {
    UTKAST, TILLEGSUTBETALING, OPPGJORT, TILBAKEKREVING, TILLEGGSUTBETALING_FEILET, TILLEGGSUTBETALING_UTBETALT;

    override fun isUbehandlet() = when (this) {
        UTKAST -> true
        TILBAKEKREVING, OPPGJORT, TILLEGSUTBETALING, TILLEGGSUTBETALING_UTBETALT, TILLEGGSUTBETALING_FEILET -> false
    }

    fun isSendtInn() = !isUbehandlet()
}
