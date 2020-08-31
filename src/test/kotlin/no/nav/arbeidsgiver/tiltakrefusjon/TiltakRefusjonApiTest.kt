package no.nav.arbeidsgiver.tiltakrefusjon

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.arbeidsgiver.tiltakrefusjon.domain.Refusjon
import no.nav.security.token.support.test.JwtTokenGenerator
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import javax.servlet.http.Cookie


@SpringBootTest
@ActiveProfiles("local")
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TiltakRefusjonApiTest(
        @Autowired val refusjonRepository: RefusjonRepository,
        @Autowired val mapper: ObjectMapper,
        @Autowired val mockMvc: MockMvc
) {

    lateinit var cookie: Cookie

    @BeforeAll
    fun setUpBeforeAll() {
        val userToken = JwtTokenGenerator.createSignedJWT("").serialize()
        cookie = Cookie("localhost-idtoken", userToken)
    }

    @BeforeEach
    fun setUp() {
        refusjonRepository.saveAll(toRefusjoner())
    }

    @Test
    fun `Henter alle refusjonene`() {
        val json = sendRequest(get(REQUEST_MAPPING))
        val liste = mapper.readValue(json, object : TypeReference<List<Refusjon?>?>() {})
        assertEquals(2, liste!!.size)
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
        val feriedagerOppdatert = refusjon.feriedager.plus(1)
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
}