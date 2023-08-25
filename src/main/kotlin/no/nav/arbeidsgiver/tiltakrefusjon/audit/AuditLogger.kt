package no.nav.arbeidsgiver.tiltakrefusjon.audit

import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.events.AuditEntry

interface AuditLogger {
    fun logg(event: AuditEntry)
}