package no.nav.arbeidsgiver.tiltakrefusjon

import no.nav.arbeidsgiver.tiltakrefusjon.domain.Refusjon
import org.junit.Assert.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.client.getForEntity
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.test.context.ActiveProfiles
import java.net.URI

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("local")
class TiltakRefusjonApiTest(
        @Autowired val restTemplate: TestRestTemplate,
        @Autowired val refusjonRepository: RefusjonRepository
) {

    @BeforeEach
    fun setUp(){
        refusjonRepository.saveAll(toRefusjoner());
    }

    @Test
    fun `Henter alle refusjonene`() {
        val result = restTemplate.getForEntity<List<Refusjon>>(URI.create(REQUEST_MAPPING));
        assertEquals(2, result.body?.size);
    }

    @Test
    fun `Henter refusjon med id`() {
        val ID = "2"
        val result = restTemplate.getForEntity<Refusjon>(URI.create("$REQUEST_MAPPING/$ID"));
        assertEquals(ID, result.body?.id);
    }

    @Test
    fun `Oppdaterer refusjon med id`() {
        var refusjon = enRefusjon();
        val feriedagerOppdatert = refusjon.feriedager?.plus(1)!!
        refusjon.feriedager = feriedagerOppdatert

        var oppdatertRefusjon = restTemplate.exchange(URI.create("$REQUEST_MAPPING"), HttpMethod.PUT, HttpEntity(refusjon), Refusjon::class.java).body
        assertEquals(refusjon.id, oppdatertRefusjon!!.id)
        assertEquals(feriedagerOppdatert, oppdatertRefusjon!!.feriedager)
    }

    @Test
    fun `Oppdaterer ikke med ukjent id`() {
        refusjonRepository.deleteById("1")
        val ukjentRefusjon = enRefusjon()
        val statuskode = restTemplate.exchange(URI.create("$REQUEST_MAPPING"), HttpMethod.PUT, HttpEntity(ukjentRefusjon), String::class.java).statusCode
        assertEquals(400, statuskode.value())
    }
}