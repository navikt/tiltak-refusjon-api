package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import no.nav.arbeidsgiver.tiltakrefusjon.AdminController
import no.nav.arbeidsgiver.tiltakrefusjon.Topics
import no.nav.arbeidsgiver.tiltakrefusjon.enRefusjon
import no.nav.arbeidsgiver.tiltakrefusjon.etTilskuddsgrunnlag
import no.nav.arbeidsgiver.tiltakrefusjon.utils.Now
import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.assertj.core.api.Assertions
import org.json.JSONObject
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.test.EmbeddedKafkaBroker
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.kafka.test.utils.KafkaTestUtils
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDate

@ActiveProfiles("local")
@SpringBootTest(properties = ["tiltak-refusjon.kafka.enabled=true"])
@EmbeddedKafka(partitions = 1, topics = [Topics.REFUSJON_ENDRET_STATUS])
@DirtiesContext
class RefusjonEndretStatusTest(
    @Autowired
    val refusjonRepository: RefusjonRepository,
    @Autowired
    val embeddedKafka: EmbeddedKafkaBroker,
    @Autowired
    val adminController: AdminController

    ) {
    lateinit var consumer: Consumer<String, String>
    @BeforeEach
    fun setup() {
        val consumerProps = KafkaTestUtils.consumerProps("testGroup", "false", embeddedKafka)
        consumerProps[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java
        consumerProps[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java

        val cf: ConsumerFactory<String, String> = DefaultKafkaConsumerFactory(consumerProps)
        consumer = cf.createConsumer()
        embeddedKafka.consumeFromAnEmbeddedTopic(consumer, Topics.REFUSJON_ENDRET_STATUS)
    }

    @Test
    fun `Kafkamelding skal produseres når en refusjon endrer status`() {
        // Nå klar for innsending
        val refusjon = enRefusjon(
            etTilskuddsgrunnlag().copy(
                tilskuddFom = Now.localDate().minusMonths(1),
                tilskuddTom = Now.localDate().minusDays(1)
            )
        )
        Now.fixedDate(LocalDate.now().plusMonths(3))
        refusjon.settTilUtgåttHvisMulig()
        refusjonRepository.save(refusjon)
        val record = KafkaTestUtils.getSingleRecord(consumer, Topics.REFUSJON_ENDRET_STATUS)
        val json = JSONObject(record.value())
        Assertions.assertThat(json.getString("status")).isEqualTo("UTGÅTT")
        Assertions.assertThat(json.getString("bedriftNr")).isEqualTo("999999999")
        Assertions.assertThat(json.getString("avtaleId")).isNotNull
        Assertions.assertThat(json.getString("refusjonId")).isNotNull
        Now.resetClock()
    }
}
