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
                ABAC,
                1000,
                Duration.ofMinutes(30)
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

const val ABAC = "abac-cache"