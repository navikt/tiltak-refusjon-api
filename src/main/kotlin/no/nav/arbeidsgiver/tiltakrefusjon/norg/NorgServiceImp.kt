package no.nav.arbeidsgiver.tiltakrefusjon.norg

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
@Profile("dev-gcp", "prod-gcp")
class NorgServiceImp(@Qualifier("anonymProxyRestTemplate") val restTemplate: RestTemplate, val properties: NorgProperties) : NorgService {
    val log: Logger = LoggerFactory.getLogger(javaClass)

    override fun hentEnhetNavn(enhet: String): String? {
        val uri = properties.uri + "/enhet/{enhet}"
        try {
            val norgResponse = restTemplate.getForEntity(
                uri,
                NorgEnhetResponse::class.java,
                mapOf("enhet" to enhet)
            )
            return norgResponse.body?.navn
        } catch (e: Exception) {
            log.error("Kall mot Norg feilet", e)
            return null
        }
    }
}
