package no.nav.arbeidsgiver.tiltakrefusjon.altinn

import no.nav.arbeidsgiver.tiltakrefusjon.Feilkode
import no.nav.arbeidsgiver.tiltakrefusjon.FeilkodeException
import no.nav.arbeidsgiver.tiltakrefusjon.utils.flatUtHierarki
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.postForObject

@Service
class AltinnTilgangsstyringService(
    val altinnTilgangsstyringProperties: AltinnTilgangsstyringProperties,
    @Qualifier("påVegneAvArbeidsgiverAltinn3RestTemplate")
    val restTemplateAltinn3: RestTemplate
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val ALTINN_2_ADRESSESPERRE = "${altinnTilgangsstyringProperties.adressesperreServiceCode}:${altinnTilgangsstyringProperties.adressesperreServiceEdition}"
    private val ALTINN_3_ADRESSESPERRE = "nav_tiltak_adressesperre"

    fun hentInntektsmeldingEllerRefusjonTilganger(): Set<Organisasjon> {
        return heltAltinnTilganger().tilGammeltFormat().also {
            logger.debug("Refusjon-tilganger for arbeidsgiver: {}", it.size)
        }
    }

    fun heltAltinnTilganger(): List<AltinnTilgang> {
        val altinnTilgangerRequest = AltinnTilgangerRequest(
            filter = Filter(
                altinn3Tilganger = setOf("nav_tiltak_tiltaksrefusjon"),
                inkluderSlettede = false
            )
        )
        val response = kallAltinn3(altinnTilgangerRequest)
        logger.debug("Respons fra altinn: {}", response)
        val løvnoderOgParents = flatUtHierarki(response)
        return løvnoderOgParents.also {
            logger.debug("Altinn-tilganger for arbeidsgiver: {}", it.size)
        }
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

    fun hentAdressesperreTilganger(): Set<Organisasjon> {
        return heltAltinnTilganger()
            .filter { it.altinn3Tilganger.contains(ALTINN_3_ADRESSESPERRE) || it.altinn2Tilganger.contains(ALTINN_2_ADRESSESPERRE) }
            .tilGammeltFormat()
            .also {
                logger.debug("Adressesperre-tilganger for arbeidsgiver: {}", it.size)
            }
    }

}

private fun List<AltinnTilgang>.tilGammeltFormat(): Set<Organisasjon> =
    this.flatMap { org ->
        listOf(
            Organisasjon(
                organizationNumber = org.orgnr,
                name = org.navn,
                organizationForm = org.organisasjonsform,
                type = "Enterprise",
                status = if (org.erSlettet) "Inactive" else "Active",
                parentOrganizationNumber = null
            )
        ) + org.underenheter.map { underenhet ->
            Organisasjon(
                organizationNumber = underenhet.orgnr,
                name = underenhet.navn,
                organizationForm = underenhet.organisasjonsform,
                type = "Business",
                status = if (underenhet.erSlettet) "Inactive" else "Active",
                parentOrganizationNumber = org.orgnr
            )
        }
    }.toSet()
