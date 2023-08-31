package no.nav.arbeidsgiver.tiltakrefusjon.audit

import no.nav.arbeidsgiver.tiltakrefusjon.Topics
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.events.AuditEntry
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@ConditionalOnProperty("tiltak-refusjon.kafka.enabled")
@Component
class AuditKafkaLogger(
    val auditKafkaTemplate: KafkaTemplate<String, AuditEntry>
) : AuditLogger {
    val log: Logger = LoggerFactory.getLogger(javaClass)

    override fun logg(event: AuditEntry) {
        auditKafkaTemplate.send(Topics.AUDIT_HENDELSE, event)
            .whenComplete { _, ex ->
                if (ex != null) {
                    log.error(
                        "Audit-hendelse kunne ikke sendes til Kafka topic {}",
                        Topics.AUDIT_HENDELSE,
                        ex
                    )
                }
            }
    }
}