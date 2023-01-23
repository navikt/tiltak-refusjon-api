package no.nav.arbeidsgiver.tiltakrefusjon.dokgen

import com.fasterxml.jackson.databind.ObjectMapper
import io.mockk.mockk
import no.nav.arbeidsgiver.tiltakrefusjon.`Siri Hansen`
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Test

internal class DokgenServiceTest () {

    @Test
    fun testing_av_parsing_av_satser () {

        val ref = `Siri Hansen`()
        val refusjonPdf = RefusjonTilPDFMapper.tilPDFdata(ref)
        val dokenservice = DokgenService(
            DokgenProperties(null), ObjectMapper(), mockk()
        )
        assertDoesNotThrow{
            dokenservice.gangOppSatserMed100(refusjonPdf)

        }
    }
}