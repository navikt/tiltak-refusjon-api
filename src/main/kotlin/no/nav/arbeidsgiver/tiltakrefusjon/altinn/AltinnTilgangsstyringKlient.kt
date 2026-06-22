package no.nav.arbeidsgiver.tiltakrefusjon.altinn

import no.nav.arbeidsgiver.tiltakrefusjon.Feilkode
import no.nav.arbeidsgiver.tiltakrefusjon.FeilkodeException
import no.nav.arbeidsgiver.tiltakrefusjon.caching.ALTINN_3_CACHE
import no.nav.arbeidsgiver.tiltakrefusjon.utils.flatUtHierarki
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.cache.annotation.Cacheable
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.postForObject

@Component
class AltinnTilgangsstyringKlient(
    val altinnTilgangsstyringProperties: AltinnTilgangsstyringProperties,
    @Qualifier("påVegneAvArbeidsgiverAltinn3RestTemplate")
    val restTemplateAltinn3: RestTemplate
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Cacheable(ALTINN_3_CACHE)
    @Retryable(
        backoff = Backoff(
            delay = 100,
            maxDelay = 1000,
            multiplier = 2.0
        )
    )
    fun heltAltinnTilganger(fnr: String): List<AltinnTilgang> {
        val altinnTilgangerRequest = AltinnTilgangerRequest(
            filter = Filter(
                altinn3Tilganger = setOf("nav_tiltak_tiltaksrefusjon"),
                inkluderSlettede = false
            )
        )
        val response = kallAltinn3(altinnTilgangerRequest)
        return flatUtHierarki(response)
    }

    fun kallAltinn3(altinnTilgangerRequest: AltinnTilgangerRequest): List<AltinnTilgang> {
        val response = try {
            restTemplateAltinn3.postForObject<AltinnTilgangerResponse>(
                altinnTilgangsstyringProperties.arbeidsgiverAltinnTilgangerUri,
                altinnTilgangerRequest
            ).hierarki
        } catch (exception: RuntimeException) {
            logger.error("Feil ved henting av Altinn-tilganger fra arbeidsgiver-altinn-tilganger", exception)
            throw FeilkodeException(Feilkode.ALTINN)
        }
        return response
    }
}
