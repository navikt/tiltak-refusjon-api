package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

data class RefusjonKlarMelding(val avtaleId: String, val varselType: VarselType)

enum class VarselType {
    KLAR,
    REVARSEL
}
