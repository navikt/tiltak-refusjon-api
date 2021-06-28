package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("local")
@SpringBootTest()
@DirtiesContext
internal class RefusjonGodkjentMeldingTest{


    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Test
    fun `generer en json`(){


        val refusjonGodkjentMelding: RefusjonGodkjentMelding = RefusjonGodkjentMelding(20)

        val value:String = objectMapper.writeValueAsString(refusjonGodkjentMelding)


        assertThat(value).isEqualTo("{\"beløp\":20}")

    }
}