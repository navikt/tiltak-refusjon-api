package no.nav.arbeidsgiver.tiltakrefusjon.autorisering

data class Tilgangsattributter(
    val kontonr: String?,
    val skjermet: Boolean,
    val diskresjonskode: Diskresjonskode?
)
