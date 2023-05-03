package no.nav.arbeidsgiver.tiltakrefusjon.utils

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class KidValidatorTest{

    @Test
    fun `Test invalid kid`(){
        assertThrows<Exception> { KidValidator("123") }
    }

    @Test
    fun `Test valid kid`(){
        assertDoesNotThrow{KidValidator("2345676")}
    }
}