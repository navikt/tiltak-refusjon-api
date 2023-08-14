package no.nav.arbeidsgiver.tiltakrefusjon.audit

import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.events.AuditEntry
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@ConditionalOnProperty("tiltak-refusjon.kafka.enabled", havingValue = "false", matchIfMissing = true)
@Component
class AuditVoidLogger: AuditLogger {
    override fun logg(event: AuditEntry) {}
}