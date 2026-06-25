package no.nav.arbeidsgiver.tiltakrefusjon.varsling

import java.time.LocalDate

data class RefusjonVarselMelding(
    val avtaleId: String,
    val tilskuddsperiodeId: String,
    val varselType: VarselType,
    val fristForGodkjenning: LocalDate?,
    val avtaleNr: Int,
    val løpenummer: Int,
    val refusjonsnummer: String,
    val tilskuddFom: LocalDate,
    val tilskuddTom: LocalDate,
)

enum class VarselType {
    KLAR,
    REVARSEL,
    FRIST_FORLENGET,
    KORRIGERT
}
