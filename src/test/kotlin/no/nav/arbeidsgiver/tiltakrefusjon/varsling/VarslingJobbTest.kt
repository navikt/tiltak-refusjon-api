package no.nav.arbeidsgiver.tiltakrefusjon.varsling


import com.ninjasquad.springmockk.MockkBean
import no.nav.arbeidsgiver.tiltakrefusjon.Topics
import no.nav.arbeidsgiver.tiltakrefusjon.hendelseslogg.HendelsesloggRepository
import no.nav.arbeidsgiver.tiltakrefusjon.leader.LeaderPodCheck
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.RefusjonRepository
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.test.context.ActiveProfiles


@ActiveProfiles("local")
@SpringBootTest(properties = ["tiltak-refusjon.kafka.enabled=true"])
@EmbeddedKafka(partitions = 1, topics = [Topics.TILTAK_VARSEL])
class VarslingJobbTest {

    @MockkBean
    lateinit var refusjonRepositoryMock: RefusjonRepository

    @MockkBean
    lateinit var varslingRepositoryMock: VarslingRepository

    @Autowired
    lateinit var kafkaTemplate: KafkaTemplate<String, RefusjonVarselMelding>


    lateinit var refusjonVarselProducer: RefusjonVarselProducer

    @Autowired
    lateinit var leaderPodCheck: LeaderPodCheck

    @Autowired
    lateinit var hendelsesloggRepositoryMock: HendelsesloggRepository

    @BeforeEach
    fun init() {
        refusjonVarselProducer = RefusjonVarselProducer(kafkaTemplate, varslingRepositoryMock)
    }
//
//    @Test
//    fun `testing av at varsling ikke blir sendt`() {
//
//
//        val varslingJobb =
//            VarslingJobb(refusjonRepositoryMock, varslingRepositoryMock, refusjonVarselProducer, leaderPodCheck)
//
//
//        val enRefusjon = enRefusjon()
//        enRefusjon.status = RefusjonStatus.KLAR_FOR_INNSENDING
//
//
//        every { refusjonRepositoryMock.findAllByStatus(RefusjonStatus.KLAR_FOR_INNSENDING) } returns listOf(enRefusjon)
//
//        every { varslingRepositoryMock.findAllByRefusjonId(enRefusjon.id) } returns listOf(
//            enVarsling(),
//            enVarsling(VarselType.REVARSEL)
//        )
//
//        varslingJobb.sjekkForVarslingKlar()
//
//        verify(exactly = 0) { varslingRepositoryMock.save(match { it.varselType == VarselType.KLAR }) }
//
//    }
//
//    @Test
//    fun `testing av at varsling klar blir sendt`() {
//
//
//        val varslingJobb =
//            VarslingJobb(refusjonRepositoryMock, varslingRepositoryMock, refusjonVarselProducer, leaderPodCheck)
//
//        val enRefusjon = enRefusjon()
//        enRefusjon.status = RefusjonStatus.KLAR_FOR_INNSENDING
//
//
//        every { refusjonRepositoryMock.findAllByStatus(RefusjonStatus.KLAR_FOR_INNSENDING) } returns listOf(enRefusjon)
//
//
//        every { varslingRepositoryMock.findAllByRefusjonId(enRefusjon.id) } returns emptyList()
//
//        varslingJobb.sjekkForVarslingKlar()
//
//        verify(timeout = 2000, exactly = 1) { varslingRepositoryMock.save(any()) }
//
//
//        hendelsesloggRepositoryMock.findAllByEvent("SendtVarsel").forEach { println("Eventlogg: "+it) }
//
//
//    }




}