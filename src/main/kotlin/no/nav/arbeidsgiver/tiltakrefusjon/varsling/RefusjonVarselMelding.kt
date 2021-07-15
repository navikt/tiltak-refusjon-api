package no.nav.arbeidsgiver.tiltakrefusjon.varsling

data class RefusjonVarselMelding(val avtaleId: String, val varselType: VarselType)

enum class VarselType {
    KLAR,
    REVARSEL
}
