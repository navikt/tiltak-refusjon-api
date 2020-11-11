package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test


class RefusjonsberegningRequestTest{

    @Test
    fun `skal kunne si at alt er ikke utfylt når noen felter er ikke satt`(){
        // GITT
        val request =  RefusjonsberegningRequest("00000000001"," ","2020-01-01","  ")

        // SÅ
        assertFalse(request.erUtfylt())
    }

    @Test
    fun `skal kunne si at alt er ikke utfylt`(){
        // GITT
        val request =  RefusjonsberegningRequest(" ","  ","  ","  ")

        // SÅ
        assertFalse(request.erUtfylt())
    }

    @Test
    fun `skal kunne si at alt er utfylt`(){
        // GITT
        val request =  RefusjonsberegningRequest("00000000001","99999999","2020-01-01","2020-01-01")

        // SÅ
        assertTrue(request.erUtfylt())
    }

}