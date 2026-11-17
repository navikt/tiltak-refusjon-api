package no.nav.arbeidsgiver.tiltakrefusjon.antivirus

import io.micrometer.core.instrument.MeterRegistry
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.http.InvalidMediaTypeException
import org.springframework.http.MediaType
import org.springframework.http.client.ClientHttpResponse
import org.springframework.http.client.JdkClientHttpRequestFactory
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import org.springframework.web.client.ResponseErrorHandler
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate
import tools.jackson.core.JacksonException
import tools.jackson.databind.DeserializationFeature
import tools.jackson.databind.JsonNode
import tools.jackson.databind.ObjectMapper
import java.net.URI
import java.net.http.HttpClient
import java.time.Duration

@Component
@ConfigurationProperties(prefix = "tiltak-refusjon.antivirus")
data class AntivirusProperties(var uri: URI? = null)

@Configuration
class AntivirusConfiguration {
    @Bean
    fun antivirusRestTemplate(): RestTemplate {
        val httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(3))
            .followRedirects(HttpClient.Redirect.NEVER)
            .build()
        val requestFactory = JdkClientHttpRequestFactory(httpClient).apply {
            setReadTimeout(Duration.ofSeconds(30))
        }
        return RestTemplate(requestFactory).apply {
            // Klienten sjekker status uten at RestTemplate leser en ubegrenset feilkropp.
            errorHandler = object : ResponseErrorHandler {
                override fun hasError(response: ClientHttpResponse) = false

                override fun handleError(url: URI, method: HttpMethod, response: ClientHttpResponse) = Unit
            }
        }
    }
}

@Service
class AntivirusClient(
    properties: AntivirusProperties,
    @param:Qualifier("antivirusRestTemplate") private val restTemplate: RestTemplate,
    private val objectMapper: ObjectMapper,
    private val meterRegistry: MeterRegistry,
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val uri = requireNotNull(properties.uri) { "URL til antivirus mangler" }.also {
        require(it.scheme in setOf("http", "https") && it.host != null && it.userInfo == null && it.fragment == null) {
            "Ugyldig URL til antivirus"
        }
    }

    fun skann(fil: ByteArray): AntivirusResultat {
        return try {
            val resultat = restTemplate.execute(uri, HttpMethod.PUT, { request ->
                request.headers.contentType = MediaType.APPLICATION_OCTET_STREAM
                request.headers.accept = listOf(MediaType.APPLICATION_JSON)
                request.body.write(fil)
            }, { response ->
                if (!response.statusCode.is2xxSuccessful) {
                    throw AntivirusException(AntivirusFeil.HTTP_FEIL)
                }
                if (response.headers.contentType?.isCompatibleWith(MediaType.APPLICATION_JSON) != true) {
                    throw AntivirusException(AntivirusFeil.UGYLDIG_SVAR)
                }
                val bytes = response.body.readAllBytes()
                val svar = objectMapper.readerFor(Array<ClamavSvar>::class.java)
                    .with(DeserializationFeature.FAIL_ON_TRAILING_TOKENS)
                    .readValue<Array<ClamavSvar>?>(bytes)
                    ?.singleOrNull() ?: throw AntivirusException(AntivirusFeil.UGYLDIG_SVAR)
                if (!svar.result.isString || !svar.virus.isString || !svar.error.isString) {
                    throw AntivirusException(AntivirusFeil.UGYLDIG_SVAR)
                }
                val result = svar.result.stringValue()
                val virus = svar.virus.stringValue()
                val error = svar.error.stringValue()
                if (result == "ERROR" || error.isNotEmpty()) {
                    throw AntivirusException(AntivirusFeil.SKANNEFEIL)
                }
                when {
                    result == "OK" && virus.isEmpty() -> AntivirusResultat.REN
                    result == "FOUND" && virus.isNotBlank() -> AntivirusResultat.VIRUS
                    else -> throw AntivirusException(AntivirusFeil.UGYLDIG_SVAR)
                }
            }) ?: throw AntivirusException(AntivirusFeil.UGYLDIG_SVAR)
            meterRegistry.counter("refusjon.antivirus", "resultat", resultat.name).increment()
            resultat
        } catch (e: AntivirusException) {
            feil(e.grunn)
        } catch (_: RestClientException) {
            feil(AntivirusFeil.TILKOBLINGSFEIL)
        } catch (_: JacksonException) {
            feil(AntivirusFeil.UGYLDIG_SVAR)
        } catch (_: InvalidMediaTypeException) {
            feil(AntivirusFeil.UGYLDIG_SVAR)
        }
    }

    private fun feil(grunn: AntivirusFeil): Nothing {
        meterRegistry.counter("refusjon.antivirus", "resultat", "FEIL").increment()
        logger.warn("Antivirusskanningen feilet: {}", grunn)
        throw AntivirusException(grunn)
    }

    private data class ClamavSvar(val result: JsonNode, val virus: JsonNode, val error: JsonNode)
}

enum class AntivirusResultat { REN, VIRUS }

enum class AntivirusFeil { UGYLDIG_SVAR, SKANNEFEIL, HTTP_FEIL, TILKOBLINGSFEIL }

class AntivirusException(val grunn: AntivirusFeil) : RuntimeException("Antivirusskanningen feilet: ${grunn.name}")
