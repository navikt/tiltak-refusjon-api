package no.nav.arbeidsgiver.tiltakrefusjon.antivirus

import io.micrometer.core.instrument.MeterRegistry
import org.slf4j.LoggerFactory
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
import org.springframework.web.client.ResourceAccessException
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
    private val logger = LoggerFactory.getLogger(javaClass)

    fun skann(fil: ByteArray): AntivirusResultat {
        return try {
            val headers = HttpHeaders().apply {
                contentType = MediaType.APPLICATION_OCTET_STREAM
                accept = listOf(MediaType.APPLICATION_JSON)
            }
            val response = restTemplate.exchange<Array<ClamavSvar>>(uri, HttpMethod.PUT, HttpEntity(fil, headers))
            if (!response.statusCode.is2xxSuccessful) {
                throw AntivirusException(AntivirusFeil.HTTP_FEIL)
            }
            val svar = response.body?.singleOrNull() ?: throw AntivirusException(AntivirusFeil.UGYLDIG_SVAR)
            if (svar.result == "ERROR" || svar.error.isNotEmpty()) {
                throw AntivirusException(AntivirusFeil.SKANNEFEIL)
            }
            val resultat = when (svar.result) {
                "OK" if svar.virus.isEmpty() -> AntivirusResultat.REN
                "FOUND" if svar.virus.isNotBlank() -> AntivirusResultat.VIRUS
                else -> throw AntivirusException(AntivirusFeil.UGYLDIG_SVAR)
            }
            meterRegistry.counter("refusjon.antivirus", "resultat", resultat.name).increment()
            resultat
        } catch (e: AntivirusException) {
            feil(e.grunn)
        } catch (_: RestClientResponseException) {
            feil(AntivirusFeil.HTTP_FEIL)
        } catch (_: ResourceAccessException) {
            feil(AntivirusFeil.TILKOBLINGSFEIL)
        } catch (_: RestClientException) {
            feil(AntivirusFeil.UGYLDIG_SVAR)
        }
    }

    private fun feil(grunn: AntivirusFeil): Nothing {
        meterRegistry.counter("refusjon.antivirus", "resultat", "FEIL").increment()
        logger.warn("Antivirusskanningen feilet: {}", grunn)
        throw AntivirusException(grunn)
    }

    private data class ClamavSvar(val result: String, val virus: String, val error: String)
}

enum class AntivirusResultat { REN, VIRUS }

enum class AntivirusFeil { UGYLDIG_SVAR, SKANNEFEIL, HTTP_FEIL, TILKOBLINGSFEIL }

class AntivirusException(val grunn: AntivirusFeil) : RuntimeException("Antivirusskanningen feilet: ${grunn.name}")
