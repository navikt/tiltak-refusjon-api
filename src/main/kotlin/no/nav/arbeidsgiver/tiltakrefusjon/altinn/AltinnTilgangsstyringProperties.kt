package no.nav.arbeidsgiver.tiltakrefusjon.altinn

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component
import java.net.URI

@Component
@ConfigurationProperties(prefix = "tiltak-refusjon.altinn-tilgangstyring")
data class AltinnTilgangsstyringProperties(
    var uri: URI = URI(""),
    var altinnApiKey: String = "",
    var beOmRettighetBaseUrl: String = "",
    var inntektsmeldingServiceCode: Int = 0,
    var inntektsmeldingServiceEdition: Int = 0,
    var antall: Int = 500,
    var adressesperreServiceCode: Int = 0,
    var adressesperreServiceEdition: Int = 0,
)
