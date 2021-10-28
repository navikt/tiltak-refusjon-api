package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

data class KorreksjonSendtTilUtbetalingMelding(
    val refusjonId: String,
    val avtaleNr: Int,
    val løpenummer: Int,
    val avtaleId: String,
    val tilskuddsperiodeId: String,
    val beløp: Int,
    val korreksjonsnummer: Int,
    val bedriftKontonummer: String,
    val korreksjonAvRefusjonId: String,
    val korreksjonstype: Korreksjonstype
)
