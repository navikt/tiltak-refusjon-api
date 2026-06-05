package no.nav.arbeidsgiver.tiltakrefusjon.altinn

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.arbeidsgiver.tiltakrefusjon.Feilkode
import no.nav.arbeidsgiver.tiltakrefusjon.FeilkodeException
import no.nav.arbeidsgiver.tiltakrefusjon.utils.flatUtHierarki
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
    @Qualifier("påVegneAvArbeidsgiverAltinn3RestTemplate")
    val restTemplateAltinn3: RestTemplate
) {
    private val objectMapper: ObjectMapper = ObjectMapper()
    private val logger = LoggerFactory.getLogger(javaClass)

    fun hentInntektsmeldingEllerRefusjonTilganger(): Set<Organisasjon> {
        val altinnTilgangerRequest = AltinnTilgangerRequest(
            filter = Filter(
                altinn3Tilganger = setOf("nav_tiltak_tiltaksrefusjon"),
                inkluderSlettede = false
            )
        )

        val response = kallAltinn3(altinnTilgangerRequest)
        val løvnoderOgParents = flatUtHierarki(response)
        val organisasjonerPåGammeltFormat = løvnoderOgParents.flatMap { org ->
            listOf(Organisasjon(
                organizationNumber = org.orgnr,
                name = org.navn,
                organizationForm = org.organisasjonsform,
                type = "Enterprise",
                status = if (org.erSlettet) "Inactive" else "Active",
                parentOrganizationNumber = null
            )) + org.underenheter.map { underenhet ->
                Organisasjon(
                    organizationNumber = underenhet.orgnr,
                    name = underenhet.navn,
                    organizationForm = underenhet.organisasjonsform,
                    type = "Business",
                    status = if (underenhet.erSlettet) "Inactive" else "Active",
                    parentOrganizationNumber = org.orgnr
                )
            }
        }
        return organisasjonerPåGammeltFormat.toSet()
    }

    fun kallAltinn3(altinnTilgangerRequest: AltinnTilgangerRequest): List<AltinnTilgang> {
        val response = try {
            restTemplateAltinn3.postForObject<AltinnTilgangerResponse>(
                altinnTilgangsstyringProperties.arbeidsgiverAltinnTilgangerUri,
                altinnTilgangerRequest
            ).hierarki
        } catch (exception: RuntimeException) {
            logger.error("Feil ved henting av Altinn-tilganger fra arbeidsgiver-altinn-tilganger", exception)
            throw FeilkodeException(Feilkode.ALTINN)
        }
        return response
    }

    private fun getAuthHeadersForAltinn(): HttpEntity<HttpHeaders?> {
        val headers = HttpHeaders()
        headers["APIKEY"] = altinnTilgangsstyringProperties.altinnApiKey
        return HttpEntity(headers)
    }

    fun hentAdressesperreTilganger(): Set<Organisasjon> {
        val altinnTilgangerRequest = AltinnTilgangerRequest(
            filter = Filter(
                altinn3Tilganger = setOf("nav_tiltak_adressesperre"),
                inkluderSlettede = false
            )
        )

        val response = kallAltinn3(altinnTilgangerRequest)
        val løvnoderOgParents = flatUtHierarki(response)
        val organisasjonerPåGammeltFormat = løvnoderOgParents.flatMap { org ->
            listOf(Organisasjon(
                organizationNumber = org.orgnr,
                name = org.navn,
                organizationForm = org.organisasjonsform,
                type = "Enterprise",
                status = if (org.erSlettet) "Inactive" else "Active",
                parentOrganizationNumber = null
            )) + org.underenheter.map { underenhet ->
                Organisasjon(
                    organizationNumber = underenhet.orgnr,
                    name = underenhet.navn,
                    organizationForm = underenhet.organisasjonsform,
                    type = "Business",
                    status = if (underenhet.erSlettet) "Inactive" else "Active",
                    parentOrganizationNumber = org.orgnr
                )
            }
        }
        return organisasjonerPåGammeltFormat.toSet()
    }

}
