package no.nav.arbeidsgiver.tiltakrefusjon.dokgen

import lombok.Data
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component
import java.net.URI

@Component
@ConfigurationProperties(prefix = "tiltak-refusjon.dokgen")
data class DokgenProperties (var uri: URI? = null)
