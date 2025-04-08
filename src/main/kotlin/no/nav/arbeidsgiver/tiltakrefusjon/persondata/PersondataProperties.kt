package no.nav.arbeidsgiver.tiltakrefusjon.persondata

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "tiltak-refusjon.pdl-api")
data class PersondataProperties(
    var uri: String = "",
)
