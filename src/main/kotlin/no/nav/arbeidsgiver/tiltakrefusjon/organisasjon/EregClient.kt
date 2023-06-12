package no.nav.arbeidsgiver.tiltakrefusjon.organisasjon

import EregOrganisasjon
import io.micrometer.observation.annotation.Observed
import no.nav.arbeidsgiver.tiltakrefusjon.Feilkode
import no.nav.arbeidsgiver.tiltakrefusjon.FeilkodeException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate
import java.util.UUID

/**
 * Ereg Team @Slack #dv-team-arbeidsforhold
 * Swagger: https://modapp-q0.adeo.no/ereg/api/swagger-ui.html#/organisasjon.v1/hentOrganisasjonUsingGET
 */
@Service
@Observed
class EregClient(val eregProperties: EregProperties, val anonymProxyRestTemplate: RestTemplate) {
    val log = LoggerFactory.getLogger(EregClient::class.java)

    fun hentVirksomhet(bedriftNr: String): Virksomhet {
        return try {
            val virksomhet = anonymProxyRestTemplate.exchange("${eregProperties.uri}/api/v1/organisasjon/${bedriftNr}?inkluderHierarki=true", HttpMethod.GET, hentHeader(), EregOrganisasjon::class.java).body
                ?: throw FeilkodeException(Feilkode.TEKNISK_FEIL_EREGOPPSLAG_FANT_IKKE_BEDRIFT)
            virksomhet.tilDomeneObjekt()
        }catch (e: RestClientException){
            log.error("Kall til Ereg med bedrift nummer ${bedriftNr} feiler med en exception: ", e)
            throw KallTilEregException("Feil ved kall til Ereg-tjenesten med bedriftsnummer: ${bedriftNr}")
        }
    }

    private fun hentHeader(): HttpEntity<String> {
        val headers = HttpHeaders()
        headers["Nav-Consumer-Id"] = eregProperties.consumerId
        headers["Nav-Call-Id"] = UUID.randomUUID().toString()
        val body = null
        return HttpEntity(body, headers)
    }

}