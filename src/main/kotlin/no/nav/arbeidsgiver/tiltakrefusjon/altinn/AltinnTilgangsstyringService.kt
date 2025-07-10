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
import org.springframework.web.client.postForObject
import org.springframework.web.util.UriComponentsBuilder

@Service
class AltinnTilgangsstyringService(
    val altinnTilgangsstyringProperties: AltinnTilgangsstyringProperties,
    @Qualifier("påVegneAvArbeidsgiverAltinnRestTemplate")
    val restTemplate: RestTemplate,
    @Qualifier("påVegneAvArbeidsgiverAltinn3RestTemplate")
    val restTemplateAltinn3: RestTemplate
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

    private fun hentTilganger(
        fnr: String,
        serviceCode: Int,
        serviceEdition: Int
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

    fun hentInntektsmeldingEllerRefusjonTilganger(): Set<Organisasjon> {
        val altinnTilgangerRequest = AltinnTilgangerRequest(
            filter = Filter(
                altinn2Tilganger = setOf(altinnTilgangsstyringProperties.inntektsmeldingsKodeAltinn2()),
                altinn3Tilganger = setOf("nav_tiltak_tiltaksrefusjon"),
                inkluderSlettede = false
            )
        )

        val response =  try {
            restTemplateAltinn3.postForObject<AltinnTilgangerResponse>(
                altinnTilgangsstyringProperties.arbeidsgiverAltinnTilgangerUri,
                altinnTilgangerRequest
            ).hierarki
        } catch (exception: RuntimeException) {
            logger.error("Feil ved henting av Altinn-tilganger fra arbeidsgiver-altinn-tilganger", exception)
            throw FeilkodeException(Feilkode.ALTINN)
        }


        val organisasjonerPåGammeltFormat = response.flatMap { org ->
            val hovedenhet = Organisasjon(
                organizationNumber = org.orgnr,
                name = org.navn,
                organizationForm = org.organisasjonsform,
                type = if (org.organisasjonsform == "BEDR") "Business" else "Enterprise", // TODO: Verifiser dette med fager.
                status = "Active", // Assuming all organizations are active TODO: Verifiser dette med fager.
                parentOrganizationNumber = response.find { parentOrg ->
                    parentOrg.underenheter?.any { underenhet -> underenhet.orgnr == org.orgnr } == true
                }?.orgnr ?: ""
            )

            val underenheter = org.underenheter.map { underenhet ->
                Organisasjon(
                    organizationNumber = underenhet.orgnr,
                    name = underenhet.navn,
                    organizationForm = underenhet.organisasjonsform,
                    type = if (underenhet.organisasjonsform == "BEDR") "Business" else "Enterprise", // TODO: Verifiser dette med fager.
                    status = "Active", // Assuming all organizations are active TODO: Verifiser dette med fager.
                    parentOrganizationNumber = org.orgnr
                )
            }

            listOf(hovedenhet) + underenheter
        }.toSet()

        return organisasjonerPåGammeltFormat
    }

    fun hentInntektsmeldingTilganger(fnr: String): Set<Organisasjon> {
        val organisasjoner = hentTilganger(
            fnr,
            serviceCode = altinnTilgangsstyringProperties.inntektsmeldingServiceCode,
            serviceEdition = altinnTilgangsstyringProperties.inntektsmeldingServiceEdition
        )
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
