package no.nav.arbeidsgiver.tiltakrefusjon.autorisering

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "tiltak-refusjon.abac-tilgangstyring")
class TilgangskontrollConfig {
    lateinit var uri: String
}