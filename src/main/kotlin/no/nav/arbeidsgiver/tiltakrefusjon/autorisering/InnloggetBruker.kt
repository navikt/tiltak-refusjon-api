package no.nav.arbeidsgiver.tiltakrefusjon.autorisering

import no.nav.arbeidsgiver.tiltakrefusjon.altinn.Organisasjon

data class InnloggetBruker (
        val identifikator: String,
        val altinnOrganisasjoner: Set<Organisasjon>
)

