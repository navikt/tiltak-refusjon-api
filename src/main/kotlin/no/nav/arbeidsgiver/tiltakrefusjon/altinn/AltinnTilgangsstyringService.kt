package no.nav.arbeidsgiver.tiltakrefusjon.altinn

import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.net.URI

@Service
class AltinnTilgangsstyringService(val altinnTilgangsstyringProperties: AltinnTilgangsstyringProperties) {

    private val restTemplate:RestTemplate = RestTemplate()

    fun hentTilganger(fnr: String): Array<Organisasjon> {
        return restTemplate.exchange(
                URI("http://localhost:8090/altinn-tilgangsstyring?subject=10000000000&serviceCode=&serviceEdition="),
                HttpMethod.GET,
                getAuthHeadersForAltinn(),
                Array<Organisasjon>::class.java).body
                ?: return emptyArray()

    }

    private fun getAuthHeadersForAltinn(): HttpEntity<HttpHeaders?>? {
        val headers = HttpHeaders()
        //headers.setBearerAuth("token")
       // headers["X-NAV-APIKEY"] = altinnTilgangsstyringProperties.apiGwApiKey
        //headers["APIKEY"] = altinnTilgangsstyringProperties.altinnApiKey
        return HttpEntity(headers)
    }
}