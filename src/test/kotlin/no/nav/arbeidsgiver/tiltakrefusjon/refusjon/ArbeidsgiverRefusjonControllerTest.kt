package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import com.github.guepardoapps.kulid.ULID
import io.mockk.every
import io.mockk.mockk
import no.nav.arbeidsgiver.tiltakrefusjon.RessursFinnesIkkeException
import no.nav.arbeidsgiver.tiltakrefusjon.`Suzanna Hansen`
import no.nav.arbeidsgiver.tiltakrefusjon.autorisering.InnloggetArbeidsgiver
import no.nav.arbeidsgiver.tiltakrefusjon.autorisering.InnloggetBrukerService
import no.nav.arbeidsgiver.tiltakrefusjon.dokgen.DokgenService
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import java.time.Instant

class ArbeidsgiverRefusjonControllerTest{


    lateinit var controller: ArbeidsgiverRefusjonController
    var innlogetServiceMock = mockk<InnloggetBrukerService>()
    var dokgenService =    mockk<DokgenService>()
    var innloggetArbeidsgiver =    mockk<InnloggetArbeidsgiver>()
    @Before
    fun setup(){
        controller = ArbeidsgiverRefusjonController(innlogetServiceMock,dokgenService)
    }


    @Test
    fun `sistEndret skal IKKEkaste en exception om NY endring tidspunkt er NY når arbeidsgiver godkjenner`(){
        every{innlogetServiceMock.hentInnloggetArbeidsgiver()} returns innloggetArbeidsgiver
        every{innloggetArbeidsgiver.finnRefusjon(any())} returns `Suzanna Hansen`()
        every{innloggetArbeidsgiver.godkjenn(any(),any())} returns Unit
        every{dokgenService.refusjonPdf(any())} returns ByteArray(1)

        assertDoesNotThrow  {controller.godkjenn("refusjon-ID",Instant.now())  }
    }
    @Test
    fun `sistEndret skal kaste en exception om NY endring tidspunkt er ELDRE enn nåværende når arbeidsgiver godkjenner`(){
        every{innlogetServiceMock.hentInnloggetArbeidsgiver()} returns innloggetArbeidsgiver
        every{innloggetArbeidsgiver.finnRefusjon(any())} returns `Suzanna Hansen`()
        every{innloggetArbeidsgiver.godkjenn(any(),any())} returns Unit
        every{dokgenService.refusjonPdf(any())} returns ByteArray(1)

        assertThrows<Exception> {controller.godkjenn("refusjon-ID",Instant.now(),)  }
    }

    @Test
    fun `test at pdf controller endepunkt ikke spitter ut noe om den ikke finnes`(){
        assertThat(controller.hentPDF("").body).isNull()
    }
    @Test
    fun `test at pdf controller endepunkt returnerer pdf som bytearray`(){

        every{innlogetServiceMock.hentInnloggetArbeidsgiver()} returns innloggetArbeidsgiver
        every{innloggetArbeidsgiver.finnRefusjon(any())} returns `Suzanna Hansen`()
        every{dokgenService.refusjonPdf(any())} returns ByteArray(1)


        val forventetHeaders = HttpHeaders()
        forventetHeaders.contentType = MediaType.APPLICATION_PDF
        forventetHeaders[HttpHeaders.CONTENT_DISPOSITION] = "inline; filename=Refusjon om " + `Suzanna Hansen`().tilskuddsgrunnlag.tiltakstype.name + ".pdf"
        forventetHeaders.contentLength = 1

        assertThat(controller.hentPDF(ULID.random()).headers).isEqualTo(forventetHeaders)
        assertThat(controller.hentPDF(ULID.random()).body).isNotEmpty()

    }

    @Test
    fun `test at pdf controller endepunkt ikke finner refusjon`(){

        every{innlogetServiceMock.hentInnloggetArbeidsgiver()} returns innloggetArbeidsgiver
        every{innloggetArbeidsgiver.finnRefusjon(any())} throws RessursFinnesIkkeException()


        assertThrows<RessursFinnesIkkeException> {controller.hentPDF(ULID.random())  }

    }

}