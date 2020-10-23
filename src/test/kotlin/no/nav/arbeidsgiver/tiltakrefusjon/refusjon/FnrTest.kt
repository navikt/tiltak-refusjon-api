package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import no.nav.arbeidsgiver.tiltakrefusjon.exceptions.RefusjonException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class FnrTest {
    @Test
    fun `gitt et tom fnr skal det kastes en exception`() {
        assertThrows(RefusjonException::class.java,{
            val fnr:Fnr = Fnr("")
        });
    }

    @Test
    fun `gitt kort fnr skal det kastes en exception`() {
        assertThrows(RefusjonException::class.java) {
            Fnr("123")
        };
    }

    @Test
    fun `gitt fnr med flere enn 11 tall skal det kastes en exception`() {
        assertThrows(RefusjonException::class.java,{
            Fnr("1234567890123")
        });
    }

    @Test
    fun `gitt fnr med bokstaver og tall skal det kastes en exception`() {
        assertThrows(RefusjonException::class.java,{
            Fnr("1123z5678901")
        });
    }

    @Test
    fun `gitt fnr andre ting enn tall skal det kastes en exception`() {
        assertThrows(RefusjonException::class.java,{
           Fnr("12345678900 ")
        });
    }

    @Test fun `gitt gyldig fnr skal true returneres`(){
        val gyldigFnrMed11Tall = "01234567890"
        assertThat(Fnr(gyldigFnrMed11Tall).verdi).isEqualTo(gyldigFnrMed11Tall)
    }
}