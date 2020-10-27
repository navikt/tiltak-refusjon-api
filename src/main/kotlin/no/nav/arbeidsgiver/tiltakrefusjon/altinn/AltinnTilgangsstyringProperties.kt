package no.nav.arbeidsgiver.tiltakrefusjon.altinn

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component
import java.net.URI

@Component
@ConfigurationProperties(prefix = "tiltak-refusjon.altinn-tilgangstyring")
data class AltinnTilgangsstyringProperties(
        var uri: URI = URI(""),
        var proxyUri: URI = URI(""),
        var altinnApiKey: String = "",
        var apiGwApiKey: String = "",
        var beOmRettighetBaseUrl: String = "",
        var serviceCode: Int = 0,
        var serviceEdition: Int = 0
)