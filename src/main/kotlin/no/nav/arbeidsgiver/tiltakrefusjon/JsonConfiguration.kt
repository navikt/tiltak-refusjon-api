package no.nav.arbeidsgiver.tiltakrefusjon

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.hibernate5.Hibernate5Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class JsonConfiguration {
    @Bean
    fun objectMapper(): ObjectMapper {
        val hibernate5Module = Hibernate5Module()
        hibernate5Module.disable(Hibernate5Module.Feature.USE_TRANSIENT_ANNOTATION)
        val mapper = ObjectMapper()
        mapper.registerModule(hibernate5Module)
        mapper.registerModule(JavaTimeModule())
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        mapper.registerModule(KotlinModule())
        return mapper
    }
}