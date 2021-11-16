package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

data class KorreksjonSendtTilUtbetalingMelding(
    val korreksjonId: String,
    val avtaleNr: Int,
    val løpenummer: Int,
    val avtaleId: String,
    val tilskuddsperiodeId: String,
    val beløp: Int,
    val korreksjonsnummer: Int,
    val bedriftKontonummer: String,
    val korrigererRefusjonId: String,
    val kostnadssted: String
)
