package no.nav.arbeidsgiver.tiltakrefusjon.grunnbelop

import no.nav.arbeidsgiver.tiltakrefusjon.caching.CacheConfig
import no.nav.arbeidsgiver.tiltakrefusjon.caching.FEM_G
import org.slf4j.LoggerFactory
import org.springframework.cache.CacheManager
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.time.LocalDate
import java.util.*

@Service
class GrunnbelopService(val grunnbelopClient: GrunnbelopClient, val cacheManager: CacheManager) {
    val logger = LoggerFactory.getLogger(GrunnbelopService::class.java)
    val cacheKey = FEM_G

    fun grunnbelopForDato(dato: LocalDate): Int {
        val grunnbelopMap: TreeMap<LocalDate, Int>? = cacheManager.getCache(FEM_G)?.get(cacheKey, {
            logger.info("Cache miss for grunnbeløp, henter fra g.nav")
            grunnbelopClient.alleGrunnbelop()
        })
        return grunnbelopMap?.lowerEntry(dato)?.value ?: throw IllegalStateException("Fant ikke grunnbeløp for dato $dato")
    }

    /**
     * Her prøver vi å forsikre oss om at cachen oppdaterer seg i en separat skedulering for å unngå at brukere blir
     * truffet av potensielle forsinkelser.
     */
    @Scheduled(fixedDelayString = "PT24H")
    fun oppdaterGrunnbelopCache() {
        try {
            logger.debug("Henter grunnbeløp fra g.nav og oppdaterer cache")
            cacheManager.getCache(FEM_G)?.put(
                cacheKey,
                grunnbelopClient.alleGrunnbelop()
            )
        } catch (e: Exception) {
            logger.error("Feil ved oppdatering av cachen for grunnbeløp", e)
        }
    }
}

fun main() {
    val cacheManager = CacheConfig().cacheManager()
    val service = GrunnbelopService(GrunnbelopClient(RestTemplate()), cacheManager)
    println("I første kall må cache oppdateres i requesten: " + service.grunnbelopForDato(LocalDate.of(2025, 5, 1)))
    cacheManager.getCache(FEM_G)?.evict(FEM_G)
    println("Etter en evict må cache oppdateres i requesten: " + service.grunnbelopForDato(LocalDate.of(2025, 5, 1)))
    cacheManager.getCache(FEM_G)?.evict(FEM_G)
    println("Evicted? " + (cacheManager.getCache(FEM_G)?.get(FEM_G) == null))
    service.oppdaterGrunnbelopCache()
    println("I tredje kall har vi oppdatert cache allerede og vil ikke treffe cache igjen: " + service.grunnbelopForDato(LocalDate.of(2025, 5, 1)))
    println("Ei heller fjerde: " + service.grunnbelopForDato(LocalDate.of(2024, 5, 1)))
    println("Eller femte: " + service.grunnbelopForDato(LocalDate.of(2023, 5, 1)))
}
