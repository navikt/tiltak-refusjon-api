package no.nav.arbeidsgiver.tiltakrefusjon.audit

import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.Korreksjon
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.Refusjon
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.events.AuditEntry
import org.slf4j.MDC
import org.springframework.http.HttpMethod
import java.net.URI
import java.time.Instant

interface AuditLogger {
    fun logg(event: AuditEntry)

    fun logg(brukerId: String, melding: String, uri: URI, metode: HttpMethod, vararg refusjoner: Refusjon) {
        val oppslagsTid = Instant.now()
        val traceId = MDC.get("traceId")
        refusjoner.map { it.deltakerFnr }.distinct().forEach {
            logg(AuditEntry(brukerId, it, oppslagsTid, melding, uri, metode, traceId))
        }
    }

    fun logg(brukerId: String, melding: String, uri: URI, metode: HttpMethod, vararg korreksjoner: Korreksjon) {
        val oppslagsTid = Instant.now()
        val traceId = MDC.get("traceId")
        korreksjoner.map { it.deltakerFnr }.distinct().forEach {
            logg(AuditEntry(brukerId, it, oppslagsTid, melding, uri, metode, traceId))
        }
    }
}