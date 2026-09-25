package no.nav.arbeidsgiver.tiltakrefusjon

import tools.jackson.databind.ObjectMapper
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.RefusjonService
import no.nav.arbeidsgiver.tiltakrefusjon.tilskuddsperiode.TilskuddsperiodeGodkjentMelding
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ActiveProfiles("local")
@SpringBootTest
@DirtiesContext
internal class AdminControllerTest {

    @Autowired
    lateinit var retryController: AdminController
    @MockitoBean
    lateinit var refusjonService: RefusjonService
    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Test
    fun `behandle godkjent tilskuddsperiode`(){
        val godkjentTilskuddsperiodeMelding =
            """
            {"avtaleId":"77ef828e-426f-4587-b662-f4b94667b1ee","tilskuddsperiodeId":"4654860f-247f-41b0-b983-9d3e8575b512","avtaleInnholdId":"e1045708-bfea-4751-8e7a-0a9814bee1e6","tiltakstype":"SOMMERJOBB","deltakerFornavn":"Duck Mark","deltakerEtternavn":"Hansen",
            "deltakerFnr":"12345678901","veilederNavIdent":"F043211","bedriftNavn":"Duck FIX ","arbeidsgiverFornavn":"Arne",
            "arbeidsgiverEtternavn":"Arbeidsgiver","arbeidsgiverTlf":"41111111", "bedriftNr":"123456789",
            "tilskuddsbeløp":3287,"tilskuddFom":"2021-07-05","tilskuddTom":"2021-07-17","feriepengerSats":0.102,
            "otpSats":0.02,"arbeidsgiveravgiftSats":0.141,"lønnstilskuddsprosent":50,"avtaleNr":33732,"løpenummer":1,"enhet":"0235","beslutterNavIdent":"J111142",
            "godkjentTidspunkt": "2021-07-10T00:00:00.000000", "mentorTimelonn" : null, "mentorAntallTimer" : null}
            """

        whenever(refusjonService.opprettRefusjon(any())).thenReturn(enRefusjon())

        retryController.opprettRefusjon(objectMapper.readValue(godkjentTilskuddsperiodeMelding, TilskuddsperiodeGodkjentMelding::class.java))

        verify(refusjonService).opprettRefusjon(
            org.mockito.kotlin.check {
                assert(it.avtaleId == "77ef828e-426f-4587-b662-f4b94667b1ee")
            }
        )



    }
}
