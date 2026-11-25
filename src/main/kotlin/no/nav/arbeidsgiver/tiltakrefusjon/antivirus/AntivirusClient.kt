package no.nav.arbeidsgiver.tiltakrefusjon.antivirus

import io.micrometer.core.instrument.MeterRegistry
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.http.client.JdkClientHttpRequestFactory
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestClientResponseException
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.exchange
import java.net.URI
import java.net.http.HttpClient
import java.time.Duration

@Configuration
class AntivirusConfiguration {
    /**
     * HTTP-klient for antivirusskanning.
     *
     * Eksplisitte tidsavbrudd tilpasset små filer.
     * Read timeout inkluderer både opplasting og venting på skanneresultatet.
     */
    @Bean
    fun antivirusRestTemplate(): RestTemplate {
        val httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(3))
            .followRedirects(HttpClient.Redirect.NEVER)
            .build()
        val requestFactory = JdkClientHttpRequestFactory(httpClient).apply {
            setReadTimeout(Duration.ofSeconds(30))
        }
        return RestTemplate(requestFactory)
    }
}

@Service
class AntivirusClient(
    @param:Value("\${tiltak-refusjon.antivirus.uri}") private val uri: URI,
    @param:Qualifier("antivirusRestTemplate") private val restTemplate: RestTemplate,
    private val meterRegistry: MeterRegistry,
) {
    fun skann(fil: ByteArray): AntivirusResultat {
        return try {
            val headers = HttpHeaders().apply {
                contentType = MediaType.APPLICATION_OCTET_STREAM
                accept = listOf(MediaType.APPLICATION_JSON)
            }
            val response = restTemplate.exchange<Array<ClamavSvar>>(uri, HttpMethod.PUT, HttpEntity(fil, headers))
            if (!response.statusCode.is2xxSuccessful) {
                throw RestClientResponseException(
                    "Uventet HTTP-status fra antivirus",
                    response.statusCode.value(),
                    response.statusCode.toString(),
                    response.headers,
                    null,
                    null
                )
            }
            val svar = response.body?.singleOrNull() ?: throw RestClientException("Ugyldig svar fra antivirus")
            if (svar.result == "ERROR" || svar.error.isNotEmpty()) {
                throw SkannefeilException(svar.error)
            }
            val resultat = when (svar.result) {
                "OK" if svar.virus.isEmpty() -> AntivirusResultat.OK
                "FOUND" if svar.virus.isNotBlank() -> AntivirusResultat.VIRUS
                else -> throw RestClientException("Ugyldig svar fra antivirus")
            }
            meterRegistry.counter("refusjon.antivirus", "resultat", resultat.name).increment()
            resultat
        } catch (e: Exception) {
            meterRegistry.counter("refusjon.antivirus", "resultat", "FEIL").increment()
            throw e
        }
    }

    private data class ClamavSvar(val result: String, val virus: String, val error: String)
}

enum class AntivirusResultat { OK, VIRUS }

class SkannefeilException(val skannefeil: String) : RuntimeException("Antivirusskanningen feilet")
