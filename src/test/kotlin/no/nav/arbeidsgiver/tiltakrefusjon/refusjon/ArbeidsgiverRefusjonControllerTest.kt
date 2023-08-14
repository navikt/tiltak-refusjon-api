package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import com.github.guepardoapps.kulid.ULID
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import no.nav.arbeidsgiver.tiltakrefusjon.RessursFinnesIkkeException
import no.nav.arbeidsgiver.tiltakrefusjon.`Suzanna Hansen`
import no.nav.arbeidsgiver.tiltakrefusjon.audit.AuditVoidLogger
import no.nav.arbeidsgiver.tiltakrefusjon.autorisering.InnloggetArbeidsgiver
import no.nav.arbeidsgiver.tiltakrefusjon.autorisering.InnloggetBrukerService
import no.nav.arbeidsgiver.tiltakrefusjon.dokgen.DokgenService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.slf4j.MDC
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import java.util.*

class ArbeidsgiverRefusjonControllerTest {

    lateinit var controller: ArbeidsgiverRefusjonController

    val voidLogger = spyk<AuditVoidLogger>()
    val innloggetServiceMock = mockk<InnloggetBrukerService>()
    val dokgenService = mockk<DokgenService>()
    val innloggetArbeidsgiver = mockk<InnloggetArbeidsgiver>()

    @BeforeEach
    fun beforeEach() {
        controller = ArbeidsgiverRefusjonController(innloggetServiceMock, voidLogger, dokgenService)
        resetAuditCount()
        MDC.put("traceId", UUID.randomUUID().toString())
    }

    @AfterEach
    fun afterEach() {
        MDC.remove("traceId")
    }

    private fun resetAuditCount() {
        clearMocks(voidLogger)
        every {
            voidLogger.logg(any())
        } returns Unit
    }

    @Test
    fun `test at pdf controller endepunkt ikke spitter ut noe om den ikke finnes`() {
        assertThat(controller.hentPDF("").body).isNull()
    }

    @Test
    fun `test at pdf controller endepunkt returnerer pdf som bytearray`() {

        every { innloggetServiceMock.hentInnloggetArbeidsgiver() } returns innloggetArbeidsgiver
        every { innloggetArbeidsgiver.finnRefusjon(any()) } returns `Suzanna Hansen`()
        every { innloggetArbeidsgiver.identifikator } returns "01010112345"
        every { dokgenService.refusjonPdf(any()) } returns ByteArray(1)


        val forventetHeaders = HttpHeaders()
        forventetHeaders.contentType = MediaType.APPLICATION_PDF
        forventetHeaders[HttpHeaders.CONTENT_DISPOSITION] = "inline; filename=Refusjon om " + `Suzanna Hansen`().refusjonsgrunnlag.tilskuddsgrunnlag.tiltakstype.name + ".pdf"
        forventetHeaders.contentLength = 1

        assertThat(controller.hentPDF(ULID.random()).headers).isEqualTo(forventetHeaders)
        assertThat(controller.hentPDF(ULID.random()).body).isNotEmpty()

    }

    @Test
    fun `test at pdf controller endepunkt ikke finner refusjon`() {

        every { innloggetServiceMock.hentInnloggetArbeidsgiver() } returns innloggetArbeidsgiver
        every { innloggetArbeidsgiver.finnRefusjon(any()) } throws RessursFinnesIkkeException()


        assertThrows<RessursFinnesIkkeException> { controller.hentPDF(ULID.random()) }

    }

}