package no.nav.arbeidsgiver.tiltakrefusjon.varsling


import com.ninjasquad.springmockk.MockkBean
import io.mockk.verify
import no.nav.arbeidsgiver.tiltakrefusjon.Topics
import no.nav.arbeidsgiver.tiltakrefusjon.enRefusjon
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.RefusjonRepository
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.RefusjonStatus
import no.nav.arbeidsgiver.tiltakrefusjon.utils.Now
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDateTime

@ActiveProfiles("local")
@SpringBootTest(properties = ["tiltak-refusjon.kafka.enabled=true"])
@EmbeddedKafka(partitions = 1, topics = [Topics.TILTAK_VARSEL])
class VarslingJobbTest(
    @Autowired
    val refusjonRepository: RefusjonRepository
) {

    @MockkBean
    lateinit var varslingRepositoryMock: VarslingRepository

    @Test
    fun `testing av cronjob for varsling klar`() {
        Now.fixedDateTime(LocalDateTime.of(20222, 4, 5, 6, 59, 57))

        val enRefusjon = enRefusjon()
        enRefusjon.status = RefusjonStatus.KLAR_FOR_INNSENDING
        refusjonRepository.save(enRefusjon)
        //every { refusjonRepositoryMock.findAllByStatus(RefusjonStatus.KLAR_FOR_INNSENDING) } returns listOf(enRefusjon)

        //Thread.sleep(3000L)

        //assertThat(varslingRepository.findAllByRefusjonId(enRefusjon.id)).isNotEmpty

        verify(timeout = 30000) {varslingRepositoryMock.save(any())}

        Now.resetClock()
    }


}