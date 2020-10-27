package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.arbeidsgiver.tiltakrefusjon.enRefusjon
import no.nav.arbeidsgiver.tiltakrefusjon.refusjoner
import no.nav.security.token.support.test.JwtTokenGenerator
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultMatcher
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import javax.servlet.http.Cookie


@SpringBootTest
@ActiveProfiles("local","wiremock")
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RefusjonApiTest(
        @Autowired val refusjonRepository: RefusjonRepository,
        @Autowired val mapper: ObjectMapper,
        @Autowired val mockMvc: MockMvc
) {

    lateinit var cookie: Cookie

    @BeforeAll
    fun setUpBeforeAll() {
        val userToken = JwtTokenGenerator.createSignedJWT("").serialize()
        cookie = Cookie("aad-idtoken", userToken)
    }

    @BeforeEach
    fun setUp() {
        refusjonRepository.saveAll(refusjoner())
    }

    @Test
    fun `Henter alle refusjonene`() {
        val json = sendRequest(get(REQUEST_MAPPING))
        val liste = mapper.readValue(json, object : TypeReference<List<Refusjon?>?>() {})
        assertEquals(14, liste!!.size)
    }

    @Test
    fun `Henter refusjoner for en bedrift`() {
        // GITT
        val userToken = JwtTokenGenerator.createSignedJWT("17049223186").serialize()
        cookie = Cookie("aad-idtoken", userToken)
        val bedriftnummer = "998877665"

        // NÅR
        val json = sendRequest(get("$REQUEST_MAPPING/bedrift/$bedriftnummer"))
        val liste = mapper.readValue(json, object : TypeReference<List<Refusjon?>?>() {})

        // DA
        assertTrue(liste!!.all { it!!.bedriftnummer.equals(bedriftnummer) })
        assertEquals(4, liste!!.size)
    }

    @Test
    fun `skal ikke kunne hente refusjoner for en bedrift som personen ikke har tilgang til`() {
        // GITT
        val fnrForPerson = "07098142678"
        val userToken = JwtTokenGenerator.createSignedJWT(fnrForPerson).serialize()
        cookie = Cookie("aad-idtoken", userToken)
        val bedriftnummer = "998877665"

        // NÅR
       sendRequest(get("$REQUEST_MAPPING/bedrift/$bedriftnummer"),status().isServiceUnavailable)
    }


    @Test
    fun `Henter refusjon med id`() {
        val id = "2"
        val json = sendRequest(get("$REQUEST_MAPPING/$id"))
        val refusjon = mapper.readValue(json, Refusjon::class.java)
        assertEquals(id, refusjon.id)
    }

    @Test
    fun `Oppdaterer refusjon med id`() {
        val refusjon = enRefusjon()
        val feriedagerOppdatert = refusjon.feriedager?.plus(1)
        refusjon.feriedager = feriedagerOppdatert

        val json = sendRequest(put(REQUEST_MAPPING), refusjon)

        val oppdatertRefusjon = mapper.readValue(json, Refusjon::class.java)
        assertEquals(refusjon.id, oppdatertRefusjon!!.id)
        assertEquals(feriedagerOppdatert, oppdatertRefusjon.feriedager)
    }

    @Test
    fun `Oppdaterer ikke med ukjent id`() {
        refusjonRepository.deleteById("1")
        val ukjentRefusjon = enRefusjon()

        mockMvc.perform(
                put(REQUEST_MAPPING)
                        .content(mapper.writeValueAsString(ukjentRefusjon))
                        .contentType(MediaType.APPLICATION_JSON)
                        .cookie(cookie))
                .andExpect(status().is4xxClientError)
    }

    @Disabled("Disabled test. update localprofile, or enable cookie auth for localhost")
    @Test
    fun `Får feil hvis cookie mangler`() {
        mockMvc.perform(
                get(REQUEST_MAPPING))
                .andExpect(status().is4xxClientError)
    }

    private fun sendRequest(request: MockHttpServletRequestBuilder): String {
        return sendRequest(request, null)
    }

    private fun sendRequest(request: MockHttpServletRequestBuilder, refusjon: Refusjon?): String {

        if (refusjon != null) {
            request.content(mapper.writeValueAsString(refusjon))
        }

        return mockMvc.perform(
                request
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .cookie(cookie))
                .andExpect(status().isOk)
                .andReturn()
                .response.contentAsString
    }
    private fun sendRequest(request: MockHttpServletRequestBuilder,forventetStatus: ResultMatcher) {
        mockMvc.perform(
                request
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .cookie(cookie))
                .andExpect(forventetStatus)
    }
}
