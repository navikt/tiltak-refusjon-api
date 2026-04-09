package no.nav.arbeidsgiver.tiltakrefusjon.grunnbelop

import no.nav.arbeidsgiver.tiltakrefusjon.caching.FEM_G
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.cache.CacheManager
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.util.*

@Service
class GrunnbelopService(private val grunnbelopClient: GrunnbelopClient, private val cacheManager: CacheManager) {
    private val log: Logger = LoggerFactory.getLogger(GrunnbelopService::class.java)
    private val cacheKey = FEM_G

    fun alleGrunnbelop(): TreeMap<LocalDate, Int> {
        val grunnbelopMap: TreeMap<LocalDate, Int>? = cacheManager.getCache(FEM_G)?.get(cacheKey, {
            log.info("Cache miss for grunnbeløp, henter fra g.nav")
            grunnbelopClient.alleGrunnbelop()
        })
        // Lag kopi av cachet map for å unngå mutasjon
        return grunnbelopMap?.toMap(TreeMap())
            ?: throw IllegalStateException("Kunne ikke hente grunnbeløp fra cache eller g.nav")
    }

    /**
     * Her prøver vi å forsikre oss om at cachen oppdaterer seg i en separat skedulering for å unngå at brukere blir
     * truffet av potensielle forsinkelser.
     */
    @Scheduled(fixedDelayString = "PT24H")
    fun oppdaterGrunnbelopCache() {
        try {
            log.debug("Henter grunnbeløp fra g.nav og oppdaterer cache")
            cacheManager.getCache(FEM_G)?.put(
                cacheKey,
                grunnbelopClient.alleGrunnbelop()
            )
        } catch (e: Exception) {
            log.error("Feil ved oppdatering av cachen for grunnbeløp", e)
        }
    }
}
