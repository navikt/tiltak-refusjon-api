package no.nav.arbeidsgiver.tiltakrefusjon.autorisering

import no.nav.arbeidsgiver.tiltakrefusjon.utils.ConditionalOnPropertyNotEmpty
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.net.URI

@Service
@ConditionalOnPropertyNotEmpty("tiltak-refusjon.graph-api.uri")
class GraphApiServiceImpl(
        @Qualifier("påVegneAvSaksbehandlerGraphRestTemplate")
        val påVegneAvSaksbehandlerGraphRestTemplate: RestTemplate,
        @Value("\${tiltak-refusjon.graph-api.uri}") val graphApiUri: URI
) : GraphApiService {
    override fun hent(): GraphApiService.GraphApiResponse {
        return påVegneAvSaksbehandlerGraphRestTemplate.getForObject(graphApiUri, GraphApiService.GraphApiResponse::class.java)
                ?: throw RuntimeException("Feil ved graph api")
    }
}