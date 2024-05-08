package no.nav.arbeidsgiver.tiltakrefusjon.audit

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class AuditLogging(val value: String)
