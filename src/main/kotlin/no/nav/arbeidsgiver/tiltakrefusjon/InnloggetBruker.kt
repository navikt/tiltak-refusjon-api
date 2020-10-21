package no.nav.arbeidsgiver.tiltakrefusjon

data class InnloggetBruker (
        val identifikator: String,
        val altinnOrganisasjoner: List<Organisasjon>,
        val tilganger: List<String>
)

