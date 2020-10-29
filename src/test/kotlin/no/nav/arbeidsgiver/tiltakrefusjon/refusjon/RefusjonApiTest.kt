package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.arbeidsgiver.tiltakrefusjon.enRefusjon
import no.nav.arbeidsgiver.tiltakrefusjon.refusjoner
import no.nav.security.token.support.test.JwtTokenGenerator
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
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
@ActiveProfiles("local", "wiremock")
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RefusjonApiTest(
        @Autowired val refusjonRepository: RefusjonRepository,
        @Autowired val mapper: ObjectMapper,
        @Autowired val mockMvc: MockMvc
) {

    lateinit var navCookie: Cookie
    lateinit var arbGiverCookie: Cookie

    @BeforeAll
    fun setUpBeforeAll() {
        val navIdToken = JwtTokenGenerator.createSignedJWT("Z123456").serialize()
        navCookie = Cookie("aad-idtoken", navIdToken)
        val arbGiverToken = JwtTokenGenerator.createSignedJWT("16120102137").serialize()
        arbGiverCookie = Cookie("aad-idtoken", arbGiverToken)
    }

    @BeforeEach
    fun setUp() {
        refusjonRepository.saveAll(refusjoner())
    }

    @Test
    fun `hentAlle() er tilgjengelig for saksbehandler`() {
        val json = sendRequest(get(REQUEST_MAPPING), navCookie)
        val liste = mapper.readValue(json, object : TypeReference<List<Refusjon?>?>() {})
        assertFalse(liste!!.isEmpty())
    }

    @Test
    fun `hentAlle() - Saksbehandler har ikke leserettighet til en refusjon`() {
        val json = sendRequest(get(REQUEST_MAPPING), navCookie)
        val liste = mapper.readValue(json, object : TypeReference<List<Refusjon?>?>() {})

        assertEquals(14, liste!!.size)
        assertNull(liste.find { refusjon -> refusjon?.deltakerFnr.equals("07098142678") })
    }

    @Test
    fun `hentAlle() er utilgjengelig for arbeidsgiver`() {
        mockMvc.perform(get(REQUEST_MAPPING).contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .cookie(arbGiverCookie))
                .andExpect(status().isUnauthorized)
    }

    @Test
    fun `hentAlleMedBedriftnummer() - Arbeidsgiver henter refusjoner for en bedrift`() {
        // GITT
        val bedriftnummer = "998877665"

        // NÅR
        val json = sendRequest(get("$REQUEST_MAPPING/bedrift/$bedriftnummer"), arbGiverCookie)
        val liste = mapper.readValue(json, object : TypeReference<List<Refusjon?>?>() {})

        // DA
        assertTrue(liste!!.all { it!!.bedriftnummer.equals(bedriftnummer) })
        assertEquals(4, liste!!.size)
    }

    @Test
    fun `hentAlleMedBedriftnummer() - Saksbehandler henter refusjoner for en bedrift`() {
        // GITT
        val bedriftnummer = "998877665"

        // NÅR
        val json = sendRequest(get("$REQUEST_MAPPING/bedrift/$bedriftnummer"), navCookie)
        val liste = mapper.readValue(json, object : TypeReference<List<Refusjon?>?>() {})

        // DA
        assertTrue(liste!!.all { it!!.bedriftnummer.equals(bedriftnummer) })
        assertEquals(4, liste!!.size)
    }

    @Test
    fun `hentAlleMedBedriftnummer() - skal ikke kunne hente refusjoner for en bedrift som personen ikke har tilgang til`() {
        // GITT
        val fnrForPerson = "07098142678"
        val userToken = JwtTokenGenerator.createSignedJWT(fnrForPerson).serialize()
        val nyCookie: Cookie = Cookie("aad-idtoken", userToken)
        val bedriftnummer = "998877665"

        // NÅR
        sendRequest(get("$REQUEST_MAPPING/bedrift/$bedriftnummer"), nyCookie, status().isServiceUnavailable)
    }

    @Test
    fun `hentAlleMedBedriftnummer() - skal ikke kunne hente refusjoner for en person som saksbehandler ikke har tilgang til`() {
        // GITT
        val fnrForPerson = "07098142678"
        val bedriftnummer = "999999999"

        // NÅR
        val json = sendRequest(get("$REQUEST_MAPPING/bedrift/$bedriftnummer"), navCookie)
        val liste = mapper.readValue(json, object : TypeReference<List<Refusjon?>?>() {})
        assertNull(liste?.find { refusjon -> refusjon?.deltakerFnr.equals(fnrForPerson) })
    }


    @Test
    fun `hent() - Arbeidsgiver henter refusjon med id`() {
        val id = "2"
        val json = sendRequest(get("$REQUEST_MAPPING/$id"), arbGiverCookie)
        val refusjon = mapper.readValue(json, Refusjon::class.java)
        assertEquals(id, refusjon.id)
    }

    @Test
    fun `hent() - Arbeidsgiver mangler tilgang til refusjon med id`() {
        val id = "15"
        sendRequest(get("$REQUEST_MAPPING/$id"), arbGiverCookie, status().isUnauthorized)
    }

    @Test
    fun `hent() - Saksbehandler henter refusjon med id`() {
        val id = "2"
        val json = sendRequest(get("$REQUEST_MAPPING/$id"), navCookie)
        val refusjon = mapper.readValue(json, Refusjon::class.java)
        assertEquals(id, refusjon.id)
    }

    @Test
    fun `hent() - Saksbehandler mangler tilgang til henter refusjon med id`() {
        val id = "1"
        sendRequest(get("$REQUEST_MAPPING/$id"), navCookie, status().isUnauthorized)
    }

    @Test
    fun `Oppdaterer refusjon med id`() {
        val refusjon = enRefusjon()
        val feriedagerOppdatert = refusjon.feriedager?.plus(1)
        refusjon.feriedager = feriedagerOppdatert

        val json = sendRequest(put(REQUEST_MAPPING), navCookie, refusjon)

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
                        .cookie(navCookie))
                .andExpect(status().is4xxClientError)
    }


    @Disabled("Disabled test. update localprofile, or enable cookie auth for localhost")
    @Test
    fun `Får feil hvis cookie mangler`() {
        mockMvc.perform(
                get(REQUEST_MAPPING))
                .andExpect(status().is4xxClientError)
    }

    private fun sendRequest(request: MockHttpServletRequestBuilder, cookie: Cookie): String {
        return sendRequest(request, cookie, null)
    }

    private fun sendRequest(request: MockHttpServletRequestBuilder, cookie: Cookie, refusjon: Refusjon?): String {

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

    private fun sendRequest(request: MockHttpServletRequestBuilder, cookie: Cookie, forventetStatus: ResultMatcher) {
        mockMvc.perform(
                request
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .cookie(cookie))
                .andExpect(forventetStatus)
    }
}
