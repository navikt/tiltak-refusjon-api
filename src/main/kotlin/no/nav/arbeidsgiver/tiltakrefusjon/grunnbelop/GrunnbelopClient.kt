package no.nav.arbeidsgiver.tiltakrefusjon.grunnbelop

import tools.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.getForObject
import java.nio.file.Files
import java.nio.file.Path
import java.net.URI
import java.time.LocalDate
import java.util.*

@Component
open class GrunnbelopClient(
    @param:Value("\${tiltak-refusjon.grunnbelop.uri}") private val url: URI,
    private val noAuthRestTemplate: RestTemplate,
    private val objectMapper: ObjectMapper
) {
    @Retryable(value = [Exception::class], maxAttempts = 3, backoff = Backoff(delay = 1000))
    open fun alleGrunnbelop(): TreeMap<LocalDate, Int> {
        val response = if (url.scheme == "file") {
            Files.newBufferedReader(Path.of(url)).use { reader ->
                objectMapper.readValue(reader, Array<GrunnbelopApiResponse>::class.java)
            }
        } else {
            noAuthRestTemplate.getForObject<Array<GrunnbelopApiResponse>>(url)
        }

        return (response ?: emptyArray()).associateTo(TreeMap()) { it.dato to it.grunnbeløp }
    }
}
