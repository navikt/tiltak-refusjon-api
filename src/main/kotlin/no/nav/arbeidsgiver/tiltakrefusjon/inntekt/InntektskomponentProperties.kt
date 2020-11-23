package no.nav.arbeidsgiver.tiltakrefusjon.inntekt

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component
import java.net.URI

@Component
@ConfigurationProperties(prefix = "tiltak-refusjon.inntektskomponenten")
data class InntektskomponentProperties(
        var uri: URI = URI(""),
        var filter:String = "",
        var consumerId: String = ""
)