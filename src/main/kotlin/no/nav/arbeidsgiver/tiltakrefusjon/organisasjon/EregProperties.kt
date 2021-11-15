package no.nav.arbeidsgiver.tiltakrefusjon.organisasjon

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component
import java.net.URI

@Component
@ConfigurationProperties(prefix = "tiltak-refusjon.ereg")
data class EregProperties (
    var uri: URI = URI(""),
    var consumerId: String = ""
)