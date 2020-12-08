package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

class FeilkodeException(val feilkode: Feilkode) : RuntimeException("Feil inntruffet: $feilkode")

enum class Feilkode {
    MANGLER_BEREGNING,
    KAN_IKKE_GODKJENNE_FLERE_GANGER,
    MANGLER_ARBEIDSGIVERS_GODKJENNING
}
