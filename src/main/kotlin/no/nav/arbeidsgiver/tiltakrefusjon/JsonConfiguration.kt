package no.nav.arbeidsgiver.tiltakrefusjon

import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer
import org.springframework.boot.kafka.autoconfigure.DefaultKafkaProducerFactoryCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.support.serializer.JacksonJsonSerializer
import tools.jackson.databind.DeserializationFeature
import tools.jackson.databind.cfg.DateTimeFeature
import tools.jackson.databind.cfg.MapperBuilder
import tools.jackson.databind.introspect.DefaultAccessorNamingStrategy
import tools.jackson.databind.json.JsonMapper
import tools.jackson.datatype.hibernate7.Hibernate7Module
import tools.jackson.module.kotlin.jacksonMapperBuilder

/**
 * Felles Jackson 3-oppsett. Spring Boot bygger JsonMapper-bønnen som brukes av Spring MVC,
 * RestTemplate og Kafka-lytterne, og denne klassen justerer den.
 */
@Configuration
class JsonConfiguration {

    @Bean
    @Order(Ordered.LOWEST_PRECEDENCE)
    fun tiltakRefusjonJsonMapperCustomizer() = JsonMapperBuilderCustomizer { builder ->
        builder.medFellesOppsett()
        // Erstatter modulen Boot finner via ServiceLoader, slik at USE_TRANSIENT_ANNOTATION er av.
        builder.addModule(Hibernate7Module().disable(Hibernate7Module.Feature.USE_TRANSIENT_ANNOTATION))
    }

    @Bean
    fun kafkaJsonSerializerCustomizer() = DefaultKafkaProducerFactoryCustomizer { producerFactory ->
        @Suppress("UNCHECKED_CAST")
        (producerFactory as DefaultKafkaProducerFactory<Any, Any>)
            .setValueSerializerSupplier { JacksonJsonSerializer<Any>(kafkaJsonMapper) }
    }

    companion object {
        /**
         * Mapper for meldinger vi produserer på Kafka. Datoer skrives som tall ([2026,8,1] og epoch-sekunder),
         * slik Jackson 2 JsonSerializer gjorde, for å ikke bryte kontrakten med konsumentene.
         */
        val kafkaJsonMapper: JsonMapper = jacksonMapperBuilder()
            .medFellesOppsett()
            .enable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
            .enable(DateTimeFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS)
            .build()
    }
}

/**
 * Jackson dropper stille egenskaper som starter med æ, ø eller å (f.eks. årsak, åpnetFørsteGang)
 * med standard accessor-navngiving. withFirstCharAcceptance(true, true) godtar dem.
 */
fun <M : tools.jackson.databind.ObjectMapper, B : MapperBuilder<M, B>> B.medFellesOppsett(): B = this
    .accessorNaming(DefaultAccessorNamingStrategy.Provider().withFirstCharAcceptance(true, true))
    .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
    .disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
