package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import no.nav.arbeidsgiver.tiltakrefusjon.medFellesOppsett
import tools.jackson.module.kotlin.jacksonMapperBuilder
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.mockito.Mockito.clearInvocations
import no.nav.arbeidsgiver.tiltakrefusjon.enRefusjon
import no.nav.arbeidsgiver.tiltakrefusjon.etTilskuddsgrunnlag
import no.nav.arbeidsgiver.tiltakrefusjon.utils.ulid
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.util.Optional
import org.mockito.ArgumentMatchers.anyString

class BetalingStatusKafkaLytterTest {
    private val refusjonRepositoryMock = mock<RefusjonRepository>()
    private val korreksjonRepositoryMock = mock<KorreksjonRepository>()
    private val objectMapper = jacksonMapperBuilder().medFellesOppsett().build()
    private val lytter = BetalingStatusKafkaLytter(refusjonRepositoryMock, korreksjonRepositoryMock, objectMapper)

    @Test
    fun `setter riktig refusjon status basert på ulike betaling statuser fra Tiltak Økonomi`() {
        val refusjonUtbetalt = enRefusjon().apply { status = RefusjonStatus.SENDT_KRAV }
        val refusjonFeilet = enRefusjon().apply { status = RefusjonStatus.SENDT_KRAV }
        whenever(refusjonRepositoryMock.findById(anyString())).thenReturn(
            Optional.of(refusjonUtbetalt),
            Optional.of(refusjonFeilet)
        )
        doReturn(refusjonUtbetalt).whenever(refusjonRepositoryMock).save(any())

        val utbetaltVelykket = BetalingStatusEndringMelding(
            "T-34649-1",
            refusjonUtbetalt.id,
            null,
            "34649", 11638.0,
            1, "11350501802", BetalingStatus.UTBETALT,
            LocalDate.now()
        )
        lytter.oppdaterKorreksjonEllerRefusjonStatusBasertPåBetalingStatusFraØkonomi(
            objectMapper.writeValueAsString(utbetaltVelykket)
        )
        verify(refusjonRepositoryMock).save(argThat { status == RefusjonStatus.UTBETALT })
        clearInvocations(refusjonRepositoryMock)

        val utbetaltFeiletHendelse = utbetaltVelykket.copy(status = BetalingStatus.FEILET)
        lytter.oppdaterKorreksjonEllerRefusjonStatusBasertPåBetalingStatusFraØkonomi(
            objectMapper.writeValueAsString(utbetaltFeiletHendelse)
        )
        verify(refusjonRepositoryMock).save(argThat { status == RefusjonStatus.UTBETALING_FEILET })
    }

    @Test
    fun `setter riktig status på korreksjon basert på melding fra Tiltak Økonomi`() {
        val tilskuddsgrunnlag = etTilskuddsgrunnlag()
        val korreksjon = Korreksjon(
            korrigererRefusjonId = ulid(),
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

        whenever(korreksjonRepositoryMock.findById(anyString())).thenReturn(Optional.of(korreksjon))
        doReturn(korreksjon).whenever(korreksjonRepositoryMock).save(any())

        val utbetaltVelykket = BetalingStatusEndringMelding(
            "T-34649-1",
            null,
            korreksjon.id,
            "34649", 11638.0,
            1, "11350501802", BetalingStatus.UTBETALT,
            LocalDate.now()
        )

        lytter.oppdaterKorreksjonEllerRefusjonStatusBasertPåBetalingStatusFraØkonomi(
            objectMapper.writeValueAsString(utbetaltVelykket)
        )

        verify(korreksjonRepositoryMock).save(argThat { status == Korreksjonstype.TILLEGGSUTBETALING_UTBETALT })
    }
}
