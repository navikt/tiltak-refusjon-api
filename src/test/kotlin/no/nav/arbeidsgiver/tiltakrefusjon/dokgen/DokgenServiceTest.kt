package no.nav.arbeidsgiver.tiltakrefusjon.dokgen

import com.fasterxml.jackson.databind.ObjectMapper
import io.mockk.mockk
import no.nav.arbeidsgiver.tiltakrefusjon.pdf.RefusjonTilPDF
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.Tiltakstype
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Test

internal class DokgenServiceTest () {

    @Test
    fun testing_av_parsing_av_satser () {

        val refusjonPDF = RefusjonTilPDF(
            type = Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD,
            avtaleNr = "1234",
            deltakerFornavn = "For",
            deltakerEtternavn = "Navn",
            arbeidsgiverFornavn = "For",
            arbeidsgiverEtternavn = "Navn",
            arbeidsgiverTlf = "12345",
            sendtKravDato = "1234",
            utbetaltKravDato = "1234",
            tilskuddFom = "1234",
            tilskuddTom = "1234",
            kontonummer = "1234",
            bedriftKid = "1234",
            lønn = 25000,
            feriepengerSats = 0.12,
            feriepenger = 12300,
            otpSats = 0.14,
            tjenestepensjon = 500,
            arbeidsgiveravgiftSats = 0.15,
            arbeidsgiveravgift = 0,
            lønnstilskuddsprosent = 60,
            refusjonsbeløp = 4000,
            beregnetBeløp = 3000,
            overTilskuddsbeløp = true,
            sumUtgifter = 3000,
            tidligereUtbetalt = 3000,
            fratrekkLønnFerie = 3000,
            lønnFratrukketFerie = 100,
            tidligereRefundertBeløp = 1000,
            tilskuddsbeløp = 1000,
            forrigeRefusjonMinusBeløp = 1000,
            forrigeRefusjonsnummer = "1233",
            sumUtgifterFratrukketRefundertBeløp = 1000,
            mentorTimelonn = null,
            mentorAntallTimer = null,
            reduksjonForDelvisPeriode = null
        )
        val dokenservice = DokgenService(
            DokgenProperties(null), ObjectMapper(), mockk()
        )
        assertDoesNotThrow{
            dokenservice.gangOppSatserMed100(refusjonPDF)
        }
    }
}
