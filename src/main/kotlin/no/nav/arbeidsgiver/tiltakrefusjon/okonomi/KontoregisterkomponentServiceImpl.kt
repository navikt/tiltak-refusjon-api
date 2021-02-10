package no.nav.arbeidsgiver.tiltakrefusjon.okonomi

import no.nav.arbeidsgiver.tiltakrefusjon.okonomi.request.KontoregisterRequest
import no.nav.arbeidsgiver.tiltakrefusjon.okonomi.response.KontoregisterResponse
import no.nav.arbeidsgiver.tiltakrefusjon.utils.ConditionalOnPropertyNotEmpty
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.exchange
import java.util.UUID


@Service
@ConditionalOnPropertyNotEmpty("tiltak-refusjon.kontoregister.uri")
class KontoregisterkomponentServiceImpl(
    val properties: KontoregisterkomponentProperties,
    @Qualifier("anonymProxyRestTemplate") val restTemplate: RestTemplate
) : KontoregisterkomponentService {

    //TODO: Bedre feilhåndtering om man får tilbake at bedriften ikke finnes i registeret. Se på wiremock kall med denne feilmeldingen.
    override fun hentBankkontonummer(bedriftNr: String): String? {
        val requestEntity = lagRequest()
        val responseMedKontonummerTilBedrift = restTemplate.exchange<KontoregisterResponse>(String.format("%s/%s", properties.uri, bedriftNr), HttpMethod.POST, requestEntity).body
        val kontonummer =  responseMedKontonummerTilBedrift?.kontonr ?: throw HentingAvBankkontonummerException();
        return kontonummer;
    }

    private fun lagRequest(): HttpEntity<KontoregisterRequest> {
        val headers = HttpHeaders()
        headers["Nav-Consumer-Id"] = properties.consumerId
        headers["Nav-Call-Id"] = UUID.randomUUID().toString()
        val body = null
        return HttpEntity(body, headers)
    }
}