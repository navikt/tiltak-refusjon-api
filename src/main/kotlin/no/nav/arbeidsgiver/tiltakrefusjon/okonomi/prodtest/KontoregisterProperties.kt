package no.nav.arbeidsgiver.tiltakrefusjon.okonomi.prodtest

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component
import java.net.URI

@Component
@ConfigurationProperties(prefix = "tiltak-refusjon.kontoregister2")
data class KontoregisterProperties2(
        var uri: URI = URI(""),
        var consumerId: String = ""
)