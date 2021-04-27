package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import no.nav.arbeidsgiver.tiltakrefusjon.Topics
import org.awaitility.Awaitility
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDate
import java.util.concurrent.TimeUnit


@ActiveProfiles("local")
@SpringBootTest(properties = ["tiltak-refusjon.kafka.enabled=true"])
@EmbeddedKafka(partitions = 1, topics = [Topics.REFUSJON_GODKJENT])
@DirtiesContext
class RefusjonKafkaProducerTest {

    @Autowired
    lateinit var kafkaTemplate: KafkaTemplate<String, RefusjonGodkjentMelding>

    @MockBean
    lateinit var refusjonService: RefusjonService

    @Autowired
    lateinit var refusjonGodkjentKafkaLytter: RefusjonGodkjentKafkaLytter

    @Test
    fun `skal opprette refusjon når melding blir lest fra topic`() {
        // GITT
        val refusjonGodkjent = RefusjonGodkjentMelding("avtaleid","tspid",Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD,
            "Donald","duck","212","z123","Duck co","123",
        1000, LocalDate.now(), LocalDate.now().plusMonths(2),0.12,0.2,0.12,40,123,1)

       val refusjon= Refusjon(tilskuddsgrunnlag = Tilskuddsgrunnlag("avtaleid","tspid",
            "Donald","duck","212","z123","Duck co","123",
            LocalDate.now(), LocalDate.now().plusMonths(2),0.12,0.2,0.12,
            Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD,123,401,1,1)
            ,"102","123")
        // NÅR
        kafkaTemplate.send(Topics.REFUSJON_GODKJENT, refusjonGodkjent.tilskuddsperiodeId, refusjonGodkjent)

        // SÅ
        Awaitility.await().atMost(30, TimeUnit.SECONDS).untilAsserted { Mockito.verify(refusjonService).godkjennForArbeidsgiver(refusjon) }
    }

}