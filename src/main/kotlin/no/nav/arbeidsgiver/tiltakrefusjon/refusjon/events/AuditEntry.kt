package no.nav.arbeidsgiver.tiltakrefusjon.refusjon.events

import org.springframework.http.HttpMethod
import java.net.URI
import java.time.Instant

data class AuditEntry(
    val utførtAv: String, // Nav-ident eller fnr på arbeidsgiver
    val oppslagPå: String, // Fnr på person det gjøres oppslag på, eller organisasjon
    val oppslagUtførtTid: Instant,
    val beskrivelse: String, // Beskrivelse av hva som er gjort, bør være "menneskelig lesbar"
    val requestUrl: URI,
    val requestMethod: HttpMethod,
    val callId: String
)
