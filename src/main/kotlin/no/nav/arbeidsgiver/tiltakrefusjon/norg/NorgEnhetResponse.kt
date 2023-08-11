package no.nav.arbeidsgiver.tiltakrefusjon.norg

data class NorgEnhetResponse(
    val enhetId: Int,
    val navn: String,
    val enhetNr: String,
    val antallRessurser: Int,
    val status: String,
    val orgNivaa: String,
    val type: String,
    val organisasjonsnummer: String,
    val underEtableringDato: String,
    val aktiveringsdato: String,
    val underAvviklingDato: String,
    val nedleggelsesdato: String,
    val oppgavebehandler: Boolean,
    val versjon: Int,
    val sosialeTjenester: String,
    val kanalstrategi: String,
    val orgNrTilKommunaltNavKontor: String,
)
