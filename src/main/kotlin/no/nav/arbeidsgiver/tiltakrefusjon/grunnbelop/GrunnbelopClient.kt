package no.nav.arbeidsgiver.tiltakrefusjon.grunnbelop

import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.getForObject
import java.net.URI
import java.time.LocalDate
import java.util.*

@Component
class GrunnbelopClient(val noAuthRestTemplate: RestTemplate) {
    @Retryable(value = [Exception::class], maxAttempts = 3, backoff = Backoff(delay = 1000))
    fun alleGrunnbelop(): TreeMap<LocalDate, Int> {
        val grunnbelopTree = TreeMap<LocalDate, Int>()
        noAuthRestTemplate.getForObject<Array<GrunnbelopApiResponse>>(
            URI.create("https://g.nav.no/api/v1/historikk/grunnbeløp")
        ).forEach {
            grunnbelopTree[it.dato] = it.grunnbeløp
        }
        return grunnbelopTree
    }
}
