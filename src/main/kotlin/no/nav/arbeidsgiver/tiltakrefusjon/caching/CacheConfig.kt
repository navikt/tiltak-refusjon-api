package no.nav.arbeidsgiver.tiltakrefusjon.caching

import com.github.benmanes.caffeine.cache.Caffeine
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.caffeine.CaffeineCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Duration

@Configuration
@EnableCaching
class CacheConfig {
    @Bean
    fun cacheManager(): CacheManager {
        val cacheManager = CaffeineCacheManager()
        listOf(
            CacheDefinition(
                FEM_G,
                1,
                Duration.ofDays(2)
            )
        ).forEach{
            cacheManager.registerCustomCache(it.name, Caffeine.newBuilder()
                .expireAfterWrite(it.ttl)
                .maximumSize(it.maxSize.toLong())
                .build())
        }
        return cacheManager
    }

    data class CacheDefinition(
        val name: String,
        val maxSize: Int,
        val ttl: Duration
    )
}

const val FEM_G = "fem-g-cache"
