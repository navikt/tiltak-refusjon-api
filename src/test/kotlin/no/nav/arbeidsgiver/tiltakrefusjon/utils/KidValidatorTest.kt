package no.nav.arbeidsgiver.tiltakrefusjon.utils

import no.nav.arbeidsgiver.tiltakrefusjon.FeilkodeException
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class KidValidatorTest{

    @Test
    fun `Test invalid kid`(){
        assertThrows<FeilkodeException> { KidValidator("") }
        assertThrows<FeilkodeException> { KidValidator("   ") }
        assertThrows<FeilkodeException> { KidValidator("123") }
    }

    @Test
    fun `Test invalid kid med 000`(){
        assertThrows<FeilkodeException> { KidValidator("0") }
        assertThrows<FeilkodeException> { KidValidator("000") }
    }

    @Test
    fun `Test valid kid`(){
        assertDoesNotThrow{KidValidator("2345676")}
        assertDoesNotThrow{KidValidator("  234 56 76  ")}
    }


}