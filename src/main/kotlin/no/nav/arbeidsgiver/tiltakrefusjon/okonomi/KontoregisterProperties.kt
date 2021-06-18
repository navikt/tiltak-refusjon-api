package no.nav.arbeidsgiver.tiltakrefusjon.okonomi

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component
import java.net.URI

@Component
@ConfigurationProperties(prefix = "tiltak-refusjon.kontoregister")
data class KontoregisterProperties(
        var uri: URI = URI(""),
        var consumerId: String = ""
)