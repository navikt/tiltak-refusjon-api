import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class AdminControllerTest {

    @BeforeEach
    fun setUp() {

    }


    @Test
    fun `behandle godkjent tilskuddsperiode`(){

        val godkjentTilskuddsperiodeMelding =
            """
            {"avtaleId":"77ef828e-426f-4587-b662-f4b94667b1ee","tilskuddsperiodeId":"4654860f-247f-41b0-b983-9d3e8575b512","avtaleInnholdId":"e1045708-bfea-4751-8e7a-0a9814bee1e6","tiltakstype":"SOMMERJOBB","deltakerFornavn":"Sahil Amar","deltakerEtternavn":"Bhatti","deltakerFnr":"10030592374","veilederNavIdent":"F145324","bedriftNavn":"RIZZI FIX AS","bedriftNr":"920908179","tilskuddsbeløp":3287,"tilskuddFom":"2021-07-05","tilskuddTom":"2021-07-17","feriepengerSats":0.102,"otpSats":0.02,"arbeidsgiveravgiftSats":0.141,"lønnstilskuddsprosent":50,"avtaleNr":33732,"løpenummer":1,"enhet":"0235","beslutterNavIdent":"J104340"}
            """



    }
}