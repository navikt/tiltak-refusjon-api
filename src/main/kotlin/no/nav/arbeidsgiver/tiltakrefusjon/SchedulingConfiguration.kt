package no.nav.arbeidsgiver.tiltakrefusjon

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling

@Configuration
@EnableScheduling
@ConditionalOnProperty(prefix = "tiltak-refusjon.scheduling", name = ["enabled"], havingValue = "true", matchIfMissing = true)
class SchedulingConfiguration
