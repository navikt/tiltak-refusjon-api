package no.nav.arbeidsgiver.tiltakrefusjon.altinn

import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.Identifikator
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI

@Service
class AltinnTilgangsstyringService(val altinnTilgangsstyringProperties: AltinnTilgangsstyringProperties) {

    private val ALTINN_ORG_PAGE_SIZE:Int = 500
    private val restTemplate:RestTemplate = RestTemplate()

    /*
        TODO:
          1. Bygge riktig url til altinn
          2. legge til og teste riktig headers
     */
    fun hentTilganger(serviceCode:Int? = null, serviceEdition: Int? = null, fnr:Identifikator): Array<Organisasjon> {
        return restTemplate.exchange(
                lagAltinnUrl(serviceCode,serviceEdition,fnr),
                HttpMethod.GET,
                getAuthHeadersForAltinn(),
                Array<Organisasjon>::class.java).body
                ?: return emptyArray()
    }

    private fun lagAltinnUrl(serviceCode:Int? = null, serviceEdition: Int? = null, fnr:Identifikator):URI{
        return UriComponentsBuilder.fromUri(altinnTilgangsstyringProperties.uri)
                .queryParam("ForceEIAuthentication")
                .queryParam("subject", fnr.verdi)
                .queryParam("serviceCode", serviceCode ?: "")
                .queryParam("serviceEdition", serviceEdition ?: "")
                .build()
                .toUri();
    }

    private fun getAuthHeadersForAltinn(): HttpEntity<HttpHeaders?>? {
        val headers = HttpHeaders()
        //headers.setBearerAuth("token")
       // headers["X-NAV-APIKEY"] = altinnTilgangsstyringProperties.apiGwApiKey
        //headers["APIKEY"] = altinnTilgangsstyringProperties.altinnApiKey
        return HttpEntity(headers)
    }
}