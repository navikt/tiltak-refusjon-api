package no.nav.arbeidsgiver.tiltakrefusjon.norg

import no.nav.arbeidsgiver.tiltakrefusjon.utils.ConditionalOnPropertyNotEmpty
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Profile

@Service
@Profile("dev-gcp", "prod-gcp")
class NorgServiceImp(@Qualifier("anonymProxyRestTemplate") val restTemplate: RestTemplate, val properties: NorgProperties) : NorgService {
    val log: Logger = LoggerFactory.getLogger(javaClass)

    override fun hentEnhetNavn(enhet: String): String? {
        val uri = properties.uri + "/enhet/" + enhet
        try {
            val norgResponse = restTemplate.getForEntity(uri, NorgEnhetResponse::class.java)
            return norgResponse.body?.navn ?: null
        } catch (e: Exception) {
            log.error("Kall mot Norg feilet", e)
            return null
        }
    }
}