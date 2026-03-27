package no.nav.arbeidsgiver.tiltakrefusjon.grunnbelop

import org.springframework.beans.factory.annotation.Value
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.getForObject
import java.net.URI
import java.time.LocalDate
import java.util.*

@Component
class GrunnbelopClient(@Value("\${tiltak-refusjon.grunnbelop.uri}") private val url: URI, val noAuthRestTemplate: RestTemplate) {
    @Retryable(value = [Exception::class], maxAttempts = 3, backoff = Backoff(delay = 1000))
    fun alleGrunnbelop(): TreeMap<LocalDate, Int> =
        noAuthRestTemplate.getForObject<Array<GrunnbelopApiResponse>>(url).associateTo(TreeMap()) { it.dato to it.grunnbeløp }
}
