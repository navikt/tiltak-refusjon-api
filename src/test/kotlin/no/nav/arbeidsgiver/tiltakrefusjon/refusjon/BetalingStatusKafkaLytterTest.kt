package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.guepardoapps.kulid.ULID
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import no.nav.arbeidsgiver.tiltakrefusjon.Topics
import no.nav.arbeidsgiver.tiltakrefusjon.enRefusjon
import no.nav.arbeidsgiver.tiltakrefusjon.etTilskuddsgrunnlag
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
    @MockkBean
    lateinit var refusjonRepositoryMock: RefusjonRepository

    @MockkBean
    lateinit var korreksjonRepositoryMock: KorreksjonRepository

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Autowired
    lateinit var betalingStatusKafkaLytter: BetalingStatusKafkaLytter

    @Test
    fun `setter riktig refusjon status basert på ulike betaling statuser fra Tiltak Økonomi`() {

        val enRefusjon = enRefusjon()
        enRefusjon.status = RefusjonStatus.SENDT_KRAV
        every { refusjonRepositoryMock.findByIdOrNull(enRefusjon.id) } returns enRefusjon
        every { refusjonRepositoryMock.findAllByStatus(RefusjonStatus.KLAR_FOR_INNSENDING) } returns emptyList()
        every { refusjonRepositoryMock.save(any()) } returns enRefusjon

        val utbetaltVelykket = BetalingStatusEndringMelding(
            "T-34649-1",
            enRefusjon.id,
            null,
            "34649", 11638.0,
            1, "11350501802", BetalingStatus.UTBETALT,
            LocalDate.now()
        )
        betalingStatusKafkaLytter.oppdaterKorreksjonEllerRefusjonStatusBasertPåBetalingStatusFraØkonomi(objectMapper.writeValueAsString(utbetaltVelykket))
        verify {
            refusjonRepositoryMock.save(match {
                it.status == RefusjonStatus.UTBETALT
            })
        }

        val utbetaltFeiletHendelse = utbetaltVelykket.copy(status = BetalingStatus.FEILET)
        betalingStatusKafkaLytter.oppdaterKorreksjonEllerRefusjonStatusBasertPåBetalingStatusFraØkonomi(objectMapper.writeValueAsString(utbetaltFeiletHendelse))
        verify {
            refusjonRepositoryMock.save(match {
                it.status == RefusjonStatus.UTBETALING_FEILET
            })
        }
    }


    @Test
    fun `setter riktig status på korreksjon basert på melding fra Tiltak Økonomi`() {
        val tilskuddsgrunnlag = etTilskuddsgrunnlag()
        val korreksjon = Korreksjon(
            korrigererRefusjonId = ULID.random(),
            korreksjonsnummer = 1,
            tidligereUtbetalt = 0,
            korreksjonsgrunner = setOf(Korreksjonsgrunn.HENT_INNTEKTER_PÅ_NYTT),
            tilskuddsgrunnlag = tilskuddsgrunnlag,
            deltakerFnr = tilskuddsgrunnlag.deltakerFnr,
            bedriftNr = tilskuddsgrunnlag.bedriftNr,
            inntekterKunFraTiltaket = true,
            endretBruttoLønn = null,
            unntakOmInntekterFremitid = 1,
            annenGrunn = null,
        ).apply {
            status = Korreksjonstype.TILLEGSUTBETALING
        }

        every { korreksjonRepositoryMock.findByIdOrNull(korreksjon.id) } returns korreksjon
        every { korreksjonRepositoryMock.save(any()) } returns korreksjon

        val utbetaltVelykket = BetalingStatusEndringMelding(
            "T-34649-1",
            null,
            korreksjon.id,
            "34649", 11638.0,
            1, "11350501802", BetalingStatus.UTBETALT,
            LocalDate.now()
        )

        betalingStatusKafkaLytter.oppdaterKorreksjonEllerRefusjonStatusBasertPåBetalingStatusFraØkonomi(objectMapper.writeValueAsString(utbetaltVelykket))

        verify {
            korreksjonRepositoryMock.save(match {
                it.status == Korreksjonstype.TILLEGGSUTBETALING_UTBETALT
            })
        }
    }
}