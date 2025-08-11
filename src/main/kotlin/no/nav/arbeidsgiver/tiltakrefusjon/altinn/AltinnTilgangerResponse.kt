package no.nav.arbeidsgiver.tiltakrefusjon.altinn
// https://arbeidsgiver-altinn-tilganger.intern.dev.nav.no/swagger-ui

data class AltinnTilgangerResponse(
    val isError: Boolean,
    val hierarki: List<AltinnTilgang>,
    val orgNrTilTilganger: Map<String, Set<String>>,
    val tilgangTilOrgNr: Map<String, Set<String>>,
)

data class AltinnTilgang(
    val orgnr: String,
    val altinn3Tilganger: Set<String>,
    val altinn2Tilganger: Set<String>,
    val underenheter: List<AltinnTilgang>,
    val navn: String,
    val organisasjonsform: String,
    val erSlettet: Boolean = false,
)
