package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import io.mockk.every
import io.mockk.mockk
import no.nav.arbeidsgiver.tiltakrefusjon.RessursFinnesIkkeException
import no.nav.arbeidsgiver.tiltakrefusjon.SyntetiskeFnr
import no.nav.arbeidsgiver.tiltakrefusjon.`Suzanna Hansen`
import no.nav.arbeidsgiver.tiltakrefusjon.autorisering.InnloggetArbeidsgiver
import no.nav.arbeidsgiver.tiltakrefusjon.autorisering.InnloggetBrukerService
import no.nav.arbeidsgiver.tiltakrefusjon.dokgen.PdfgenService
import no.nav.arbeidsgiver.tiltakrefusjon.utils.ulid
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType

@SyntetiskeFnr
class ArbeidsgiverRefusjonControllerTest {

    private lateinit var controller: ArbeidsgiverRefusjonController
    private lateinit var innloggetServiceMock: InnloggetBrukerService
    private lateinit var pdfgenService: PdfgenService
    private lateinit var innloggetArbeidsgiver: InnloggetArbeidsgiver

    @BeforeEach
    fun setup() {
        innloggetServiceMock = mockk()
        pdfgenService = mockk()
        innloggetArbeidsgiver = mockk()
        controller = ArbeidsgiverRefusjonController(innloggetServiceMock, pdfgenService)
    }

    @Test
    fun `test at pdf controller endepunkt ikke spitter ut noe om den ikke finnes`() {
        assertThat(controller.hentPDF("").body).isNull()
    }

    @Test
    fun `test at pdf controller endepunkt returnerer pdf som bytearray`() {
        every { innloggetServiceMock.hentInnloggetArbeidsgiver() } returns innloggetArbeidsgiver
        every { innloggetArbeidsgiver.finnRefusjon(any()) } returns `Suzanna Hansen`()
        every { pdfgenService.refusjonPdf(any()) } returns ByteArray(1)

        val forventetHeaders = HttpHeaders()
        forventetHeaders.contentType = MediaType.APPLICATION_PDF
        forventetHeaders[HttpHeaders.CONTENT_DISPOSITION] =
            "inline; filename=Refusjon om " + `Suzanna Hansen`().refusjonsgrunnlag.tilskuddsgrunnlag.tiltakstype.name + ".pdf"
        forventetHeaders.contentLength = 1

        assertThat(controller.hentPDF(ulid()).headers).isEqualTo(forventetHeaders)
        assertThat(controller.hentPDF(ulid()).body).isNotEmpty()
    }

    @Test
    fun `test at pdf controller endepunkt ikke finner refusjon`() {
        every { innloggetServiceMock.hentInnloggetArbeidsgiver() } returns innloggetArbeidsgiver
        every { innloggetArbeidsgiver.finnRefusjon(any()) } throws RessursFinnesIkkeException()

        assertThrows<RessursFinnesIkkeException> { controller.hentPDF(ulid()) }
    }
}
