package no.nav.arbeidsgiver.tiltakrefusjon.norg

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.getForEntity

@Service
@Profile("dev-gcp", "prod-gcp")
class NorgServiceImp(
    @param:Qualifier("anonymProxyRestTemplate") private val restTemplate: RestTemplate,
    private val properties: NorgProperties
) : NorgService {
    private val log: Logger = LoggerFactory.getLogger(javaClass)

    override fun hentEnhetNavn(enhet: String): String? {
        val uri = properties.uri + "/enhet/{enhet}"
        try {
            val norgResponse = restTemplate.getForEntity<NorgEnhetResponse>(
                uri,
                mapOf("enhet" to enhet)
            )
            return norgResponse.body?.navn
        } catch (e: Exception) {
            log.error("Kall mot Norg feilet", e)
            return null
        }
    }
}
