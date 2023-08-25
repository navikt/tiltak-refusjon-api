package no.nav.arbeidsgiver.tiltakrefusjon.refusjon.events

import java.net.URI
import java.time.Instant

data class AuditEntry(
    val appNavn: String,
    val utførtAv: String, // Nav-ident eller fnr på arbeidsgiver
    val oppslagPå: String, // Fnr på person det gjøres oppslag på, eller organisasjon
    val eventType: EventType,
    val forespørselTillatt: Boolean,
    val oppslagUtførtTid: Instant,
    val beskrivelse: String, // Beskrivelse av hva som er gjort, bør være "menneskelig lesbar"
    val requestUrl: URI,
    val requestMethod: String,
    val correlationId: String
)
enum class EventType {
    CREATE, READ, UPDATE, DELETE
}