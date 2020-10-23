package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import no.nav.arbeidsgiver.tiltakrefusjon.exceptions.RefusjonException
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

internal class FnrTest {
    @Test
    fun `gitt et tom fnr skal det kastes en exception`() {
        assertThrows(RefusjonException::class.java,{
            val fnr:Fnr = Fnr("")
        });

    }
}