package no.nav.arbeidsgiver.tiltakrefusjon.audit

import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.events.AuditEntry
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@ConditionalOnProperty("tiltak-refusjon.kafka.enabled", havingValue = "false", matchIfMissing = true)
@Component
class AuditConsoleLogger: AuditLogger {

    val log = LoggerFactory.getLogger(javaClass)

    override fun logg(event: AuditEntry) {
        log.info("Audit-event: {}", event)
    }
}