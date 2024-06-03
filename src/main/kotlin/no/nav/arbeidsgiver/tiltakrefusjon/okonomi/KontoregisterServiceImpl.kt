package no.nav.arbeidsgiver.tiltakrefusjon.okonomi

import no.nav.arbeidsgiver.tiltakrefusjon.okonomi.request.KontoregisterRequest
import no.nav.arbeidsgiver.tiltakrefusjon.okonomi.response.KontoregisterResponse
import no.nav.arbeidsgiver.tiltakrefusjon.utils.ConditionalOnPropertyNotEmpty
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.exchange
import java.util.*

/*
* SLACK: PO-UTBETALING - GITHUB https://github.com/navikt/sokos-kontoregister
* */
@Service
@ConditionalOnPropertyNotEmpty("tiltak-refusjon.kontoregister.uri")
class KontoregisterServiceImpl(
    val properties: KontoregisterProperties,
    @Qualifier("sokosRestTemplate") val restTemplate: RestTemplate
) : KontoregisterService {

    val log: Logger = LoggerFactory.getLogger(javaClass)

    override fun hentBankkontonummer(bedriftNr: String): String? {
        val requestEntity = lagRequest()
        val url = "${properties.uri}/${bedriftNr}"
        try {
            log.warn("##### kall url: ${url} ");
            val responseMedKontonummerTilBedrift =
                restTemplate.exchange<KontoregisterResponse>(url, HttpMethod.GET, requestEntity)
            log.warn("##### kall headers: ${responseMedKontonummerTilBedrift.headers}");
            return responseMedKontonummerTilBedrift?.body?.kontonr
        } catch (e: RestClientException) {
            log.warn("Kontoregister call feiler", e)
        }
        return null
    }

    private fun lagRequest(): HttpEntity<KontoregisterRequest> {
        val headers = HttpHeaders()
        headers["Nav-Consumer-Id"] = properties.consumerId
        headers["Nav-Call-Id"] = UUID.randomUUID().toString()
        val body = null
        return HttpEntity(body, headers)
    }
}