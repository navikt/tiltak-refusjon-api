package no.nav.arbeidsgiver.tiltakrefusjon.tilskuddsperiode

import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.converter.StringJsonMessageConverter
import org.springframework.kafka.support.serializer.JsonSerializer
import java.util.HashMap

@ConditionalOnProperty("tiltak-refusjon.kafka.enabled")
@TestConfiguration
@EnableKafka
class AivenKafkaConfiguration {
    @Value("\${no.nav.gcp.kafka.aiven.bootstrap-servers}")
    private val bootstrapServers: String? = null
    private fun producerConfigs(): Map<String, Any?> {
        val props: MutableMap<String, Any?> = HashMap()
        props["group-id"] = "tilskudd"
        props[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = bootstrapServers
        props[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
        props[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = JsonSerializer::class.java
        return props
    }

    @Bean
    fun aivenKafkaTemplate(): KafkaTemplate<String, TilskuddsperiodeGodkjentMelding> {
        val kafkaTemplate: KafkaTemplate<String, TilskuddsperiodeGodkjentMelding> = KafkaTemplate(DefaultKafkaProducerFactory(producerConfigs()))
        kafkaTemplate.setMessageConverter(StringJsonMessageConverter())
        return kafkaTemplate
    }
}