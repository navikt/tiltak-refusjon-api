package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import io.mockk.every
import io.mockk.mockk
import no.nav.arbeidsgiver.tiltakrefusjon.RessursFinnesIkkeException
import no.nav.arbeidsgiver.tiltakrefusjon.`Suzanna Hansen`
import no.nav.arbeidsgiver.tiltakrefusjon.autorisering.InnloggetArbeidsgiver
import no.nav.arbeidsgiver.tiltakrefusjon.autorisering.InnloggetBrukerService
import no.nav.arbeidsgiver.tiltakrefusjon.dokgen.DokgenService
import no.nav.arbeidsgiver.tiltakrefusjon.utils.ulid
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType

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
    fun `test at pdf controller endepunkt ikke spitter ut noe om den ikke finnes`(){
        assertThat(controller.hentPDF("").body).isNull()
    }
    @Test
    fun `test at pdf controller endepunkt returnerer pdf som bytearray`(){

        every{innlogetServiceMock.hentInnloggetArbeidsgiver()} returns innloggetArbeidsgiver
        every{innloggetArbeidsgiver.finnRefusjonImmutable(any())} returns `Suzanna Hansen`()
        every{dokgenService.refusjonPdf(any())} returns ByteArray(1)


        val forventetHeaders = HttpHeaders()
        forventetHeaders.contentType = MediaType.APPLICATION_PDF
        forventetHeaders[HttpHeaders.CONTENT_DISPOSITION] = "inline; filename=Refusjon om " + `Suzanna Hansen`().refusjonsgrunnlag.tilskuddsgrunnlag.tiltakstype.name + ".pdf"
        forventetHeaders.contentLength = 1

        assertThat(controller.hentPDF(ulid()).headers).isEqualTo(forventetHeaders)
        assertThat(controller.hentPDF(ulid()).body).isNotEmpty()

    }

    @Test
    fun `test at pdf controller endepunkt ikke finner refusjon`(){

        every{innlogetServiceMock.hentInnloggetArbeidsgiver()} returns innloggetArbeidsgiver
        every{innloggetArbeidsgiver.finnRefusjonImmutable(any())} throws RessursFinnesIkkeException()


        assertThrows<RessursFinnesIkkeException> {controller.hentPDF(ulid())  }

    }

}