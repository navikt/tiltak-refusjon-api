package no.nav.arbeidsgiver.tiltakrefusjon.tilskuddsperiode

import com.ninjasquad.springmockk.MockkBean
import io.mockk.verify
import no.nav.arbeidsgiver.tiltakrefusjon.Topics
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.RefusjonService
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.Tiltakstype
import no.nav.arbeidsgiver.tiltakrefusjon.utils.Now
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import java.util.UUID

@ActiveProfiles("local")
@SpringBootTest(properties = ["tiltak-refusjon.kafka.enabled=true"])
@EmbeddedKafka(partitions = 1, topics = [Topics.TILSKUDDSPERIODE_GODKJENT])
@DirtiesContext
class TilskuddsperiodeLytterTest {

    @Autowired
    lateinit var kafkaTemplate: KafkaTemplate<String, TilskuddsperiodeGodkjentMelding>

    @MockkBean
    lateinit var refusjonServiceMock: RefusjonService

    @Autowired
    lateinit var tilskuddsperiodeLytter: TilskuddsperiodeKafkaLytter

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
            enhet = "1000"
        )

        // NÅR
        kafkaTemplate.send(Topics.TILSKUDDSPERIODE_GODKJENT, tilskuddMelding.tilskuddsperiodeId, tilskuddMelding)

        // SÅ
        verify(timeout = 3000) { refusjonServiceMock.opprettRefusjon(tilskuddMelding) }

    }

}