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

    private val ALTINN_ORG_PAGE_SIZE = 500
    private val restTemplate:RestTemplate = RestTemplate()

    fun hentTilganger(fnr: Identifikator): Set<Organisasjon> {
        try {
            return restTemplate.exchange(
                    lagAltinnUrl(fnr),
                    HttpMethod.GET,
                    getAuthHeadersForAltinn(),
                    Array<Organisasjon>::class.java).body?.toSet()
                    ?: return emptySet()
        }catch (exception: RuntimeException){
            throw AltinnFeilException("Altinn feil",exception)
        }
    }

    private fun lagAltinnUrl(fnr: Identifikator):URI{
        return UriComponentsBuilder.fromUri(altinnTilgangsstyringProperties.uri)
                .queryParam("ForceEIAuthentication")
                .queryParam("subject", fnr.verdi)
                .queryParam("serviceCode", altinnTilgangsstyringProperties.serviceCode)
                .queryParam("serviceEdition", altinnTilgangsstyringProperties.serviceEdition)
                .queryParam("\$top", ALTINN_ORG_PAGE_SIZE)
                .build()
                .toUri();
    }

   private fun getAuthHeadersForAltinn(): HttpEntity<HttpHeaders?>? {
        val headers = HttpHeaders()
        headers.setBearerAuth("token")
        headers["X-NAV-APIKEY"] = altinnTilgangsstyringProperties.apiGwApiKey
        headers["APIKEY"] = altinnTilgangsstyringProperties.altinnApiKey
        return HttpEntity(headers)
    }

}