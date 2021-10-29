package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import java.time.LocalDate

data class BetalingStatusEndringMelding(
    val id: String,
    val refusjonId: String,
    val avtaleNr: String,
    val beløp: Int,
    val løpenummer: Int,
    val kontonummer: String,
    val status: BetalingStatus,
    val avregningsdato: LocalDate
    ){
    fun erBetalt():Boolean{
        return BetalingStatus.UTBETALT.equals(status)
    }
}