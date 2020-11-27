package no.nav.arbeidsgiver.tiltakrefusjon.tilskudd

import org.junit.jupiter.api.Test
import org.mockito.Mockito.any
import org.mockito.Mockito.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDate
import java.util.UUID

@ActiveProfiles("local")
@SpringBootTest(properties = ["tiltak-refusjon.kafka.enabled=true"])
@DirtiesContext
@EmbeddedKafka(partitions = 1, topics = [Topics.REFUSJON])
class GodkjentTilskuddLytterTest {


    @Autowired
    lateinit var kafkaTemplate: KafkaTemplate<String, TilskuddMelding>

    @MockBean
    lateinit var beregningServiceMock: BeregningService

    @Autowired
    lateinit var godkjentTilskuddLytter: GodkjentTilskuddLytter

    @Test
    fun `skal beregne tilskudd når melding blir lest fra topic`() {


        // GITT
        val tilskuddMelding = TilskuddMelding(UUID.randomUUID(),
                UUID.randomUUID(), UUID.randomUUID(),
                "VARLIG-LØNNSTILSKUDD", "Donald",
                "Duck", "12345678901", "X123456",
                "Duck Levering AS", "99999999", 12000,
                LocalDate.now().minusDays(15), LocalDate.now())

        // NÅR
        kafkaTemplate.send(Topics.REFUSJON, tilskuddMelding.avtaleId.toString(), tilskuddMelding)

        // SÅ

        Thread.sleep(1000)
        verify(beregningServiceMock).bereg(any<TilskuddMelding>() ?: tilskuddMelding)
    }


}