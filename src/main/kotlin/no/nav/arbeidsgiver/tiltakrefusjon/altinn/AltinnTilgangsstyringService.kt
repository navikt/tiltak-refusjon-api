package no.nav.arbeidsgiver.tiltakrefusjon.altinn

import no.nav.arbeidsgiver.tiltakrefusjon.Feilkode
import no.nav.arbeidsgiver.tiltakrefusjon.FeilkodeException
import org.slf4j.LoggerFactory
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
    private val logger = LoggerFactory.getLogger(javaClass)

    fun hentTilganger(fnr: String): Set<Organisasjon> {
        val organisasjoner = HashSet<Organisasjon>()
        var merÅHente = true
        var i = 0;
        while (merÅHente) {
            val skip = altinnTilgangsstyringProperties.antall * i++
            val nyeOrg = hentFraAltinn(fnr, skip)
            organisasjoner.addAll(nyeOrg)
            merÅHente = nyeOrg.size >= altinnTilgangsstyringProperties.antall
        }
        return organisasjoner
    }

    private fun hentFraAltinn(fnr: String, skip: Int): Set<Organisasjon> {
        try {
            return restTemplate.exchange(
                lagAltinnUrl(fnr, skip),
                HttpMethod.GET,
                getAuthHeadersForAltinn(),
                Array<Organisasjon>::class.java).body?.toSet()
                ?: return emptySet()
        } catch (exception: RuntimeException) {
            logger.error("Feil med Altinn", exception)
            throw FeilkodeException(Feilkode.ALTINN)
        }
    }

    private fun lagAltinnUrl(fnr: String, skip: Int): URI {
        return UriComponentsBuilder.fromUri(altinnTilgangsstyringProperties.uri)
            .queryParam("ForceEIAuthentication")
            .queryParam("subject", fnr)
            .queryParam("serviceCode", altinnTilgangsstyringProperties.serviceCode)
            .queryParam("serviceEdition", altinnTilgangsstyringProperties.serviceEdition)
            .queryParam("\$top", altinnTilgangsstyringProperties.antall)
            .queryParam("\$skip", skip)
            .queryParam("\$filter", "Type+ne+'Person'+and+Status+eq+'Active'")
            .build()
            .toUri()
    }

    private fun getAuthHeadersForAltinn(): HttpEntity<HttpHeaders?> {
        val headers = HttpHeaders()
        headers["APIKEY"] = altinnTilgangsstyringProperties.altinnApiKey
        return HttpEntity(headers)
    }

}