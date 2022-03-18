package no.nav.arbeidsgiver.tiltakrefusjon.varsling

import java.time.LocalDate

data class RefusjonVarselMelding(val avtaleId: String, val tilskuddsperiodeId: String, val varselType: VarselType, val fristForGodkjenning: LocalDate?)

enum class VarselType {
    KLAR,
    REVARSEL,
    FRIST_FORLENGET,
    KORRIGERT
}
