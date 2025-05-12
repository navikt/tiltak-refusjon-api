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

@Service
class AltinnTilgangsstyringService(
    val altinnTilgangsstyringProperties: AltinnTilgangsstyringProperties,
    @Qualifier("påVegneAvArbeidsgiverAltinnRestTemplate")
    val restTemplate: RestTemplate,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    // URL-strengen har to "uri-variabler": fnr og skip
    private val altinnUrlString: String = UriComponentsBuilder.fromUri(altinnTilgangsstyringProperties.uri)
        .queryParam("ForceEIAuthentication")
        .queryParam("subject", "{fnr}")
        .queryParam("serviceCode", "{serviceCode}")
        .queryParam("serviceEdition", "{serviceEdition}")
        .queryParam("\$top", altinnTilgangsstyringProperties.antall)
        .queryParam("\$skip", "{skip}")
        .queryParam("\$filter", "Type+ne+'Person'+and+Status+eq+'Active'")
        .build()
        .toUriString()

    fun hentTilganger(
        fnr: String,
        serviceCode: Int = altinnTilgangsstyringProperties.serviceCode,
        serviceEdition: Int = altinnTilgangsstyringProperties.serviceEdition
    ): Set<Organisasjon> {
        val organisasjoner = HashSet<Organisasjon>()
        var merÅHente = true
        var i = 0;
        while (merÅHente) {
            val skip = altinnTilgangsstyringProperties.antall * i++
            val nyeOrg = hentFraAltinn(fnr, skip, serviceCode, serviceEdition)
            organisasjoner.addAll(nyeOrg)
            merÅHente = nyeOrg.size >= altinnTilgangsstyringProperties.antall
        }
        return organisasjoner
    }

    fun hentAdressesperreTilganger(fnr: String): Set<Organisasjon> {
        val organisasjoner = hentTilganger(
            fnr,
            serviceCode = altinnTilgangsstyringProperties.adressesperreServiceCode,
            serviceEdition = altinnTilgangsstyringProperties.adressesperreServiceEdition
        )
        return organisasjoner
    }

    private fun hentFraAltinn(
            fnr: String,
            skip: Int,
            serviceCode: Int,
            serviceEdition: Int
        ): Set<Organisasjon> {
        try {
            return restTemplate.exchange(
                altinnUrlString,
                HttpMethod.GET,
                getAuthHeadersForAltinn(),
                Array<Organisasjon>::class.java,
                mapOf(
                    "fnr" to fnr,
                    "skip" to skip,
                    "serviceCode" to serviceCode,
                    "serviceEdition" to serviceEdition
                )
            ).body?.toSet()
                ?: return emptySet()
        } catch (exception: RuntimeException) {
            logger.error("Feil med Altinn", exception)
            throw FeilkodeException(Feilkode.ALTINN)
        }
    }

    private fun getAuthHeadersForAltinn(): HttpEntity<HttpHeaders?> {
        val headers = HttpHeaders()
        headers["APIKEY"] = altinnTilgangsstyringProperties.altinnApiKey
        return HttpEntity(headers)
    }

}
