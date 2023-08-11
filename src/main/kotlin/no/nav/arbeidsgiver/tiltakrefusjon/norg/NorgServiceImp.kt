package no.nav.arbeidsgiver.tiltakrefusjon.norg

import no.nav.arbeidsgiver.tiltakrefusjon.utils.ConditionalOnPropertyNotEmpty
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Profile

@Service
@Profile("dev-gcp", "prod-gcp")
class NorgServiceImp(@Qualifier("anonymProxyRestTemplate") val restTemplate: RestTemplate, val properties: NorgProperties) : NorgService {

    override fun hentEnhetNavn(enhet: String): String? {
        val uri = properties.uri + "/enhet/" + enhet
        val norgResponse = restTemplate.getForEntity(uri, NorgEnhetResponse::class.java)
        return norgResponse.body?.navn ?: null
    }
}