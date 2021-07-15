package no.nav.arbeidsgiver.tiltakrefusjon.varsling

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "tiltak-refusjon.varslingsjobb")
data class VarslingProperties(val earlyBirds: List<String>)