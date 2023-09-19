package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import java.time.LocalDate

data class BetalingStatusEndringMelding(
    val id: String,
    val refusjonId: String?,
    val korreksjonId: String?,
    val avtaleNr: String,
    val beløp: Double,
    val løpenummer: Int,
    val kontonummer: String,
    val status: BetalingStatus,
    val avregningsdato: LocalDate
    ){
    fun erUtbetalt():Boolean{
        return BetalingStatus.UTBETALT.equals(status)
    }

    fun erForRefusjon() = refusjonId != null
    fun erForKorreksjon() = korreksjonId != null
}