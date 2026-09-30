package no.nav.arbeidsgiver.tiltakrefusjon.varsling

import no.nav.arbeidsgiver.tiltakrefusjon.enRefusjon
import no.nav.arbeidsgiver.tiltakrefusjon.enVarsling
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.RefusjonRepository
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.RefusjonStatus
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.kafka.core.KafkaTemplate
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class VarslingJobbTest {
    private val refusjonRepositoryMock = mock<RefusjonRepository>()
    private val varslingRepositoryMock = mock<VarslingRepository>()
    private val kafkaTemplate = mock<KafkaTemplate<String, RefusjonVarselMelding>>()
    private lateinit var refusjonVarselProducer: RefusjonVarselProducer

    @BeforeEach
    fun init() {
        refusjonVarselProducer = RefusjonVarselProducer(kafkaTemplate, varslingRepositoryMock)
        whenever(kafkaTemplate.send(any(), any(), any())).thenReturn(java.util.concurrent.CompletableFuture.completedFuture(mock()))
    }

    @Test
    fun `testing av at varsling ikke blir sendt`() {
        val varslingJobb = VarslingJobb(refusjonRepositoryMock, varslingRepositoryMock, refusjonVarselProducer)

        val enRefusjon = enRefusjon()
        enRefusjon.status = RefusjonStatus.KLAR_FOR_INNSENDING

        whenever(refusjonRepositoryMock.findAllByStatus(RefusjonStatus.KLAR_FOR_INNSENDING)).thenReturn(listOf(enRefusjon))
        whenever(varslingRepositoryMock.findAllByRefusjonId(enRefusjon.id)).thenReturn(listOf(
            enVarsling(),
            enVarsling(VarselType.REVARSEL)
        ))

        varslingJobb.sjekkForVarslingKlar()

        org.mockito.Mockito.verify(varslingRepositoryMock, org.mockito.Mockito.never()).save(argThat { varselType == VarselType.KLAR })
    }

    @Test
    fun `testing av at varsling klar blir sendt`() {
        val varslingJobb = VarslingJobb(refusjonRepositoryMock, varslingRepositoryMock, refusjonVarselProducer)

        val enRefusjon = enRefusjon()
        enRefusjon.status = RefusjonStatus.KLAR_FOR_INNSENDING

        whenever(refusjonRepositoryMock.findAllByStatus(RefusjonStatus.KLAR_FOR_INNSENDING)).thenReturn(listOf(enRefusjon))
        whenever(varslingRepositoryMock.findAllByRefusjonId(enRefusjon.id)).thenReturn(emptyList())

        varslingJobb.sjekkForVarslingKlar()

        verify(varslingRepositoryMock).save(any())
    }
}
