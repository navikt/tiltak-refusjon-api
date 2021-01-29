package no.nav.arbeidsgiver.tiltakrefusjon.tilskuddsperiode

import lombok.extern.slf4j.Slf4j
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.config.SslConfigs
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.serializer.JsonSerializer
import java.util.HashMap

@ConditionalOnProperty("tiltak-refusjon.kafka.enabled")
@Configuration
@Slf4j
@EnableKafka
class AivenKafkaConfiguration {
    @Value("\${no.nav.gcp.kafka.aiven.bootstrap-servers}")
    private val gcpBootstrapServers: String? = null
    private val javaKeystore = "jks"
    private val pkcs12 = "PKCS12"

    @Value("\${no.nav.gcp.kafka.aiven.truststore-path}")
    private val sslTruststoreLocationEnvKey: String? = null

    @Value("\${no.nav.gcp.kafka.aiven.truststore-password}")
    private val sslTruststorePasswordEnvKey: String? = null

    @Value("\${no.nav.gcp.kafka.aiven.keystore-path}")
    private val sslKeystoreLocationEnvKey: String? = null

    @Value("\${no.nav.gcp.kafka.aiven.keystore-password}")
    private val sslKeystorePasswordEnvKey: String? = null
    private fun producerConfigs(): Map<String, Any?> {
        val props: MutableMap<String, Any?> = HashMap()
        props["group-id"] = "tilskudd"
        props[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = gcpBootstrapServers
        props[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
        props[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = JsonSerializer::class.java
        props[CommonClientConfigs.SECURITY_PROTOCOL_CONFIG] = "SSL"
        props[SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG] = ""
        props[SslConfigs.SSL_TRUSTSTORE_TYPE_CONFIG] = javaKeystore
        props[SslConfigs.SSL_KEYSTORE_TYPE_CONFIG] = pkcs12
        props[SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG] = sslTruststoreLocationEnvKey
        props[SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG] = sslTruststorePasswordEnvKey
        props[SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG] = sslKeystoreLocationEnvKey
        props[SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG] = sslKeystorePasswordEnvKey
        return props
    }

    @Bean
    fun aivenKafkaTemplate(): KafkaTemplate<String, TilskuddsperiodeGodkjentMelding> {
        return KafkaTemplate(DefaultKafkaProducerFactory(producerConfigs()))
    }
}