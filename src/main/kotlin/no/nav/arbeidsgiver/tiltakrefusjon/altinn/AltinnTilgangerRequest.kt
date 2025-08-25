package no.nav.arbeidsgiver.tiltakrefusjon.altinn

data class AltinnTilgangerRequest(val filter: Filter)

// https://arbeidsgiver-altinn-tilganger.intern.dev.nav.no/swagger-ui
data class Filter(
    val altinn2Tilganger: Set<String> = emptySet(),
    val altinn3Tilganger: Set<String> = emptySet(),
    val inkluderSlettede: Boolean = false,
)
