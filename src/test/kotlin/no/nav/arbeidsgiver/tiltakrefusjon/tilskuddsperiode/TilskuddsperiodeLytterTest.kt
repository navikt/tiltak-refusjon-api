package no.nav.arbeidsgiver.tiltakrefusjon.tilskuddsperiode

import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.RefusjonService
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.Tiltakstype
import org.awaitility.Awaitility.await
import org.junit.jupiter.api.Test
import org.mockito.Mockito.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDate
import java.util.*
import java.util.concurrent.TimeUnit

@ActiveProfiles("local")
@SpringBootTest(properties = ["tiltak-refusjon.kafka.enabled=true"])
@EmbeddedKafka(partitions = 1, topics = [Topics.TILSKUDDSPERIODE_GODKJENT])
@DirtiesContext
class TilskuddsperiodeLytterTest {

    @Autowired
    lateinit var kafkaTemplate: KafkaTemplate<String, TilskuddsperiodeGodkjentMelding>

    @MockBean
    lateinit var refusjonService: RefusjonService

    @Autowired
    lateinit var tilskuddsperiodeLytter: TilskuddsperiodeKafkaLytter

    @Test
    fun `skal opprette refusjon når melding blir lest fra topic`() {
        // GITT
        val tilskuddMelding = TilskuddsperiodeGodkjentMelding(UUID.randomUUID().toString(),
                UUID.randomUUID().toString(), UUID.randomUUID().toString(),
                Tiltakstype.VARIG_LONNSTILSKUDD, "Donald",
                "Duck", "12345678901", "X123456",
                "Duck Levering AS", "99999999", 12000,
                LocalDate.now().minusDays(15), LocalDate.now(), 0.12, 0.02, 0.141, 60)

        // NÅR
        kafkaTemplate.send(Topics.TILSKUDDSPERIODE_GODKJENT, tilskuddMelding.tilskuddsperiodeId, tilskuddMelding)

        // SÅ
        await().atMost(15, TimeUnit.SECONDS).untilAsserted { verify(refusjonService).opprettRefusjon(tilskuddMelding) }
    }

}