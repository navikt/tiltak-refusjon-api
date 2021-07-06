package no.nav.arbeidsgiver.tiltakrefusjon.altinn

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI

@Service
class AltinnTilgangsstyringService(
    val altinnTilgangsstyringProperties: AltinnTilgangsstyringProperties,
    @Qualifier("påVegneAvArbeidsgiverAltinnRestTemplate")
    val restTemplate: RestTemplate,
) {

    private val ALTINN_ORG_PAGE_SIZE = 999

    fun hentTilganger(fnr: String): Set<Organisasjon> {
        try {
            return restTemplate.exchange(
                lagAltinnUrl(fnr),
                HttpMethod.GET,
                getAuthHeadersForAltinn(),
                Array<Organisasjon>::class.java).body?.toSet()
                ?: return emptySet()
        } catch (exception: RuntimeException) {
            throw AltinnFeilException("Altinn feil", exception)
        }
    }

    private fun lagAltinnUrl(fnr: String): URI {
        return UriComponentsBuilder.fromUri(altinnTilgangsstyringProperties.uri)
            .queryParam("ForceEIAuthentication")
            .queryParam("subject", fnr)
            .queryParam("serviceCode", altinnTilgangsstyringProperties.serviceCode)
            .queryParam("serviceEdition", altinnTilgangsstyringProperties.serviceEdition)
            .queryParam("\$top", ALTINN_ORG_PAGE_SIZE)
            .queryParam("\$filter", "Type+ne+'Person'")
            .build()
            .toUri()
    }

    private fun getAuthHeadersForAltinn(): HttpEntity<HttpHeaders?> {
        val headers = HttpHeaders()
        headers["APIKEY"] = altinnTilgangsstyringProperties.altinnApiKey
        return HttpEntity(headers)
    }

}