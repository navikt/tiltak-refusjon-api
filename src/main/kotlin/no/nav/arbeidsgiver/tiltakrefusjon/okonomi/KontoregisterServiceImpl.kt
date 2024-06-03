package no.nav.arbeidsgiver.tiltakrefusjon.okonomi

import no.nav.arbeidsgiver.tiltakrefusjon.okonomi.request.KontoregisterRequest
import no.nav.arbeidsgiver.tiltakrefusjon.okonomi.response.KontoregisterResponse
import no.nav.arbeidsgiver.tiltakrefusjon.utils.ConditionalOnPropertyNotEmpty
import no.nav.security.token.support.client.core.ClientProperties
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpRequest
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
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
    @Qualifier("sokosRestTemplate") val restTemplate: RestTemplate,
    val restTemplateBuilder: RestTemplateBuilder,
) : KontoregisterService {

    val log: Logger = LoggerFactory.getLogger(javaClass)

    override fun hentBankkontonummer(bedriftNr: String): String? {
        val url = "${properties.uri}/${bedriftNr}"
        try {
            log.warn("##### kall url: ${url} ${restTemplate} TEMPLATE CLASS: ${restTemplate.javaClass}");
            val responseMedKontonummerTilBedrift =
                restTemplate.exchange<KontoregisterResponse>(url, HttpMethod.GET)
            return responseMedKontonummerTilBedrift?.body?.kontonr
        } catch (e: RestClientException) {
            log.warn("Kontoregister call feiler", e)
        }
        return null
    }

}