package no.nav.arbeidsgiver.tiltakrefusjon.altinn

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component
import java.net.URI

@Component
@ConfigurationProperties(prefix = "tiltak-refusjon.altinn-tilgangstyring")
data class AltinnTilgangsstyringProperties(
    @Deprecated("Altinn 2 skal fases ut")
    /** URI til Team Fager sin proxy som går videre direkte til Altinn 2: https://github.com/navikt/altinn-rettigheter-proxy  */
    var uri: URI = URI(""),
    /** URI til Team Fager sitt api som støtter både Altinn 3 og 2: https://github.com/navikt/arbeidsgiver-altinn-tilganger */
    var arbeidsgiverAltinnTilgangerUri: URI = URI(""), //
    var altinnApiKey: String = "",
    var beOmRettighetBaseUrl: String = "",
    var inntektsmeldingServiceCode: Int = 0,
    var inntektsmeldingServiceEdition: Int = 0,
    var antall: Int = 500,
    var adressesperreServiceCode: Int = 0,
    var adressesperreServiceEdition: Int = 0,
)

fun AltinnTilgangsstyringProperties.inntektsmeldingsKodeAltinn2(): String {
    return "$inntektsmeldingServiceCode:$inntektsmeldingServiceEdition"
}
