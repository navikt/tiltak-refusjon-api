package no.nav.arbeidsgiver.tiltakrefusjon.autorisering

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.net.URI

@Service
class GraphApiService(
        @Qualifier("påVegneAvSaksbehandlerGraphRestTemplate")
        val påVegneAvSaksbehandlerGraphRestTemplate: RestTemplate,
        @Value("\${tiltak-refusjon.graph-api.uri}") val graphApiUri: URI
) {
    fun hentNavIdent(): NavIdent {
        val response = påVegneAvSaksbehandlerGraphRestTemplate.getForObject(graphApiUri, GraphApiResponse::class.java)
        return response?.let { NavIdent(it.onPremisesSamAccountName) } ?: throw RuntimeException("Finner ikke navident")
    }

    data class GraphApiResponse(val onPremisesSamAccountName: String)
}