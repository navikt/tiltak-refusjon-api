package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import no.nav.arbeidsgiver.tiltakrefusjon.Topics
import no.nav.arbeidsgiver.tiltakrefusjon.enRefusjon
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDate

@ActiveProfiles("local")
@SpringBootTest(properties = ["tiltak-refusjon.kafka.enabled=true"])
@EmbeddedKafka(partitions = 1, topics = [Topics.REFUSJON_ENDRET_BETALINGSSTATUS])
@DirtiesContext
class BetalingStatusKafkaLytterTest {
    @MockkBean lateinit var refusjonRepositoryMock: RefusjonRepository
    @Autowired lateinit var  objectMapper: ObjectMapper

    @Test
    fun `setter riktig refusjon status basert på ulike betaling statuser fra Tiltak Økonomi`() {

        val betalingStatusKafkaLytter = BetalingStatusKafkaLytter(refusjonRepositoryMock,objectMapper)

        val enRefusjon = enRefusjon()
        enRefusjon.status = RefusjonStatus.SENDT_KRAV
        every { refusjonRepositoryMock.findByIdOrNull(enRefusjon.id) } returns enRefusjon
        every { refusjonRepositoryMock.findAllByStatus(RefusjonStatus.KLAR_FOR_INNSENDING) } returns emptyList()
        every { refusjonRepositoryMock.save(any()) } returns enRefusjon

        val utbetaltVelykket = BetalingStatusEndringMelding("T-34649-1",
            enRefusjon.id,
            "34649",11638,
            1,"11350501802",BetalingStatus.UTBETALT,
            LocalDate.now())
        betalingStatusKafkaLytter.oppdaterRefusjonStatusBasertPåBetalingStatusFraØkonomi(objectMapper.writeValueAsString(utbetaltVelykket))
        verify { refusjonRepositoryMock.save(match{
            it.status == RefusjonStatus.UTBETALT
        }) }

        val utbetaltFeiletHendelse = utbetaltVelykket.copy(status = BetalingStatus.FEILET)
        betalingStatusKafkaLytter.oppdaterRefusjonStatusBasertPåBetalingStatusFraØkonomi(objectMapper.writeValueAsString(utbetaltFeiletHendelse))
        verify { refusjonRepositoryMock.save(match{
            it.status == RefusjonStatus.UTBETALING_FEILET
        }) }
    }
}