package no.nav.arbeidsgiver.tiltakrefusjon.tilskuddsperiode

import no.nav.arbeidsgiver.tiltakrefusjon.Topics
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.Tiltakstype
import no.nav.arbeidsgiver.tiltakrefusjon.utils.Now
import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.apache.kafka.common.serialization.StringDeserializer
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.serializer.JsonDeserializer
import org.springframework.kafka.test.EmbeddedKafkaBroker
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.kafka.test.utils.KafkaTestUtils
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.time.LocalDateTime
import java.util.*


@ActiveProfiles("local")
@SpringBootTest(properties = ["tiltak-refusjon.kafka.enabled=true"])
@EmbeddedKafka(partitions = 1, topics = [Topics.TILSKUDDSPERIODE_GODKJENT])
@ExtendWith(SpringExtension::class)
@DirtiesContext
class TilskuddsperiodeLytterTest {

    @Autowired
    lateinit var kafkaTemplate: KafkaTemplate<String, TilskuddsperiodeGodkjentMelding>

    @Autowired
    lateinit var embeddedKafkaBroker: EmbeddedKafkaBroker


    @Test
    fun `skal opprette refusjon når melding blir lest fra topic`() {
        // GITT
        val tilskuddMelding = TilskuddsperiodeGodkjentMelding(
            avtaleId = UUID.randomUUID().toString(),
            tilskuddsperiodeId = UUID.randomUUID().toString(),
            avtaleInnholdId = UUID.randomUUID().toString(),
            tiltakstype = Tiltakstype.VARIG_LONNSTILSKUDD,
            deltakerFornavn = "Donald",
            deltakerEtternavn = "Duck",
            deltakerFnr = "12345678901",
            arbeidsgiverFornavn = "Arne",
            arbeidsgiverEtternavn = "Arbeidsgiver",
            arbeidsgiverTlf = "41111111",
            veilederNavIdent = "X123456",
            bedriftNavn = "Duck Levering AS",
            bedriftNr = "99999999",
            tilskuddsbeløp = 12000,
            tilskuddFom = Now.localDate().minusDays(15),
            tilskuddTom = Now.localDate(),
            feriepengerSats = 0.12,
            otpSats = 0.02,
            arbeidsgiveravgiftSats = 0.141,
            lønnstilskuddsprosent = 60,
            avtaleNr = 3456,
            løpenummer = 3,
            resendingsnummer = null,
            enhet = "1000",
            godkjentTidspunkt = LocalDateTime.now(),
            arbeidsgiverKontonummer = "12345678908",
            arbeidsgiverKid = null,
        )

        kafkaTemplate.send(Topics.TILSKUDDSPERIODE_GODKJENT, tilskuddMelding.tilskuddsperiodeId, tilskuddMelding)
        Thread.sleep(300L)
        val consumerProps = KafkaTestUtils.consumerProps("testGroup", "true", embeddedKafkaBroker)
        consumerProps[ConsumerConfig.AUTO_OFFSET_RESET_CONFIG] = "earliest"
        val consumerFactory: ConsumerFactory<String, TilskuddsperiodeGodkjentMelding> = DefaultKafkaConsumerFactory<String, TilskuddsperiodeGodkjentMelding>(
            consumerProps,
            StringDeserializer(),
            JsonDeserializer<TilskuddsperiodeGodkjentMelding>(TilskuddsperiodeGodkjentMelding::class.java)
        )
        val consumer: Consumer<String, TilskuddsperiodeGodkjentMelding> = consumerFactory.createConsumer()
        embeddedKafkaBroker.consumeFromAnEmbeddedTopic(consumer, Topics.TILSKUDDSPERIODE_GODKJENT)
        val replies: ConsumerRecords<String, TilskuddsperiodeGodkjentMelding> = KafkaTestUtils.getRecords(consumer)
        Assertions.assertThat(replies.count()).isGreaterThanOrEqualTo(1)
    }

}
