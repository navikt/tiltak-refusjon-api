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

    private val restTemplate:RestTemplate = RestTemplate()

    fun hentTilganger(serviceCode:Int? = null, serviceEdition: Int? = null, fnr: Identifikator): Set<Organisasjon> {
        return restTemplate.exchange(
                lagAltinnUrl(hentTallString(serviceCode),hentTallString(serviceEdition),fnr),
                HttpMethod.GET,
                getAuthHeadersForAltinn(),
                Array<Organisasjon>::class.java).body?.toSet()
                ?: return emptySet()
    }

    private fun lagAltinnUrl(serviceCode:String, serviceEdition: String , fnr:Identifikator):URI{
        return UriComponentsBuilder.fromUri(altinnTilgangsstyringProperties.uri)
                .queryParam("ForceEIAuthentication")
                .queryParam("subject", fnr.verdi)
                .queryParam("serviceCode", serviceCode ?: "")
                .queryParam("serviceEdition", serviceEdition ?: "")
                .build()
                .toUri();
    }

    private fun hentTallString(verdi:Int? = null):String{
        return verdi?.toString() ?: ""
    }

    private fun getAuthHeadersForAltinn(): HttpEntity<HttpHeaders?>? {
        val headers = HttpHeaders()
        //headers.setBearerAuth("token")
       // headers["X-NAV-APIKEY"] = altinnTilgangsstyringProperties.apiGwApiKey
        //headers["APIKEY"] = altinnTilgangsstyringProperties.altinnApiKey
        return HttpEntity(headers)
    }
}