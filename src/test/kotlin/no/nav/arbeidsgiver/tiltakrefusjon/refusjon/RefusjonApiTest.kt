package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.nimbusds.jwt.JWTClaimsSet
import no.nav.arbeidsgiver.tiltakrefusjon.Feilkode
import no.nav.arbeidsgiver.tiltakrefusjon.refusjoner
import no.nav.security.token.support.test.JwkGenerator
import no.nav.security.token.support.test.JwtTokenGenerator
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultMatcher
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.nio.charset.StandardCharsets
import java.util.*
import javax.servlet.http.Cookie


@SpringBootTest
@ActiveProfiles("local")
@AutoConfigureMockMvc
@AutoConfigureWireMock(port = 8090)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RefusjonApiTest(
        @Autowired val refusjonRepository: RefusjonRepository,
        @Autowired val mapper: ObjectMapper,
        @Autowired val mockMvc: MockMvc
) {

    private final val TOKEN_X_COOKIE_NAVN = "tokenx-token"
    private final val AAD_COOKIE_NAVN = "aad-token"

    val navCookie = Cookie(AAD_COOKIE_NAVN, lagTokenForNavId("Z123456"))
    val arbGiverCookie = Cookie(TOKEN_X_COOKIE_NAVN, lagTokenForFnr("16120102137"))

    @BeforeEach
    fun setUp() {
        refusjonRepository.saveAll(refusjoner())
    }

    @AfterEach
    fun tearDown() {
        refusjonRepository.deleteAll()
    }

    @Test
    fun `hentAlle() er tilgjengelig for saksbehandler`() {
        val json = sendRequest(get(REQUEST_MAPPING_SAKSBEHANDLER_REFUSJON), navCookie)
        val liste = mapper.readValue(json, object : TypeReference<List<Refusjon>>() {})
        assertFalse(liste.isEmpty())
    }

    @Test
    fun `hentAlle() - Saksbehandler har ikke leserettighet til en refusjon`() {
        val json = sendRequest(get(REQUEST_MAPPING_SAKSBEHANDLER_REFUSJON), navCookie)
        val liste = mapper.readValue(json, object : TypeReference<List<Refusjon>>() {})

        assertEquals(4, liste.size)
        assertNull(liste.find { it.deltakerFnr == "07098142678" })
    }

    @Test
    fun `hentAlle() er utilgjengelig for arbeidsgiver`() {
        mockMvc.perform(get(REQUEST_MAPPING_ARBEIDSGIVER_REFUSJON)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .cookie(arbGiverCookie))
                .andExpect(status().isBadRequest)
    }

    @Test
    fun `hentAlleMedBedriftnummer() - Arbeidsgiver henter refusjoner for en bedrift`() {
        // GITT
        val bedriftNr = "998877665"

        // NÅR
        val json = sendRequest(get("$REQUEST_MAPPING_ARBEIDSGIVER_REFUSJON?bedriftNr=$bedriftNr"), arbGiverCookie)
        val liste = mapper.readValue(json, object : TypeReference<List<Refusjon>>() {})

        // DA
        assertTrue(liste.all { it.bedriftNr == bedriftNr })
        assertEquals(2, liste.size)
    }

    @Test
    fun `hentAlleMedBedriftnummer() - Saksbehandler henter refusjoner for en bedrift`() {
        // GITT
        val bedriftNr = "998877665"

        // NÅR
        val json = sendRequest(get("$REQUEST_MAPPING_SAKSBEHANDLER_REFUSJON?bedriftNr=$bedriftNr"), navCookie)
        val liste = mapper.readValue(json, object : TypeReference<List<Refusjon>>() {})

        // SÅ
        assertThat(liste).allMatch { it.bedriftNr == bedriftNr }
        assertEquals(2, liste.size)
    }

    @Test
    fun `hentAlleMedBedriftnummer() - skal ikke kunne hente refusjoner for en person som saksbehandler ikke har tilgang til`() {
        // GITT
        val fnrForPerson = "07098142678"
        val bedriftNr = "999999999"

        // NÅR
        val json = sendRequest(get("$REQUEST_MAPPING_SAKSBEHANDLER_REFUSJON?bedriftNr=$bedriftNr"), navCookie)
        val liste = mapper.readValue(json, object : TypeReference<List<Refusjon>>() {})
        assertNull(liste.find { refusjon -> refusjon.deltakerFnr == fnrForPerson })
    }

    @Test
    fun `hent() - Arbeidsgiver henter refusjon med id`() {
        val id = refusjonRepository.findAll().find { it.deltakerFnr == "07098142678" }?.id

        val json = sendRequest(get("$REQUEST_MAPPING_ARBEIDSGIVER_REFUSJON/$id"), arbGiverCookie)
        val refusjon = mapper.readValue(json, Refusjon::class.java)
        assertEquals(id, refusjon.id)
    }

    @Test
    fun `hent() - Arbeidsgiver mangler tilgang til refusjon med id`() {
        val id = refusjonRepository.findAll().find { it.deltakerFnr == "23119409195" }?.id

        sendRequest(get("$REQUEST_MAPPING_ARBEIDSGIVER_REFUSJON/$id"), arbGiverCookie, status().isUnauthorized)
    }

    @Test
    fun `hent() - Saksbehandler henter refusjon med id`() {
        val id = refusjonRepository.findAll().find { it.deltakerFnr == "28128521498" }?.id

        val json = sendRequest(get("$REQUEST_MAPPING_SAKSBEHANDLER_REFUSJON/$id"), navCookie)
        val refusjon = mapper.readValue(json, Refusjon::class.java)
        assertEquals(id, refusjon.id)
    }

    @Test
    fun `hent() - Saksbehandler mangler tilgang til henter refusjon med id`() {
        val id = refusjonRepository.findAll().find { it.deltakerFnr == "07098142678" }?.id
        sendRequest(get("$REQUEST_MAPPING_SAKSBEHANDLER_REFUSJON/$id"), navCookie, status().isUnauthorized)
    }

    @Test
    fun `Får feil hvis cookie mangler arbeidsgiver`() {
        mockMvc.perform(
                get(REQUEST_MAPPING_ARBEIDSGIVER_REFUSJON))
                .andExpect(status().isUnauthorized)
    }

    @Test
    fun `Får feil hvis cookie mangler saksbehandler`() {
        mockMvc.perform(
                get(REQUEST_MAPPING_SAKSBEHANDLER_REFUSJON))
                .andExpect(status().isUnauthorized)
    }

    @Test
    fun `Arbeidsgiver kan gjøre inntektsoppslag, og hente refusjon med inntektsgrunnlag, og godkjenne`() {
        val id = refusjonRepository.findAll().find { it.deltakerFnr == "28128521498" }?.id

        // Inntektsoppslag
        sendRequest(post("$REQUEST_MAPPING_ARBEIDSGIVER_REFUSJON/$id/inntektsoppslag"), arbGiverCookie)
        val refusjonEtterInntektsgrunnlag = hentRefusjon(id)
        assertThat(refusjonEtterInntektsgrunnlag.inntektsgrunnlag).isNotNull
        assertThat(refusjonEtterInntektsgrunnlag.beregning?.refusjonsbeløp).isPositive()

        // Godkjenn
        sendRequest(post("$REQUEST_MAPPING_ARBEIDSGIVER_REFUSJON/$id/godkjenn"), arbGiverCookie)
        val refusjonEtterGodkjennelse = hentRefusjon(id)
        assertThat(refusjonEtterGodkjennelse.godkjentAvArbeidsgiver).isNotNull()
    }

    @Test
    fun `feilkode setter riktig header og gir statuskode 400`() {
        val id = refusjonRepository.findAll().find { it.deltakerFnr == "28128521498" }?.id

        // Godkjenn
        mockMvc.perform(
                post("$REQUEST_MAPPING_ARBEIDSGIVER_REFUSJON/$id/godkjenn")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .cookie(arbGiverCookie))
                .andExpect(status().isBadRequest)
                .andExpect(header().string("feilkode", Feilkode.UGYLDIG_STATUS.toString()))
    }

    private fun hentRefusjon(id: String?): Refusjon {
        val json = sendRequest(get("$REQUEST_MAPPING_ARBEIDSGIVER_REFUSJON/$id"), arbGiverCookie)
        return mapper.readValue(json, Refusjon::class.java)
    }

    private fun sendRequest(request: MockHttpServletRequestBuilder, cookie: Cookie): String {
        return sendRequest(request, cookie, null)
    }

    private fun sendRequest(request: MockHttpServletRequestBuilder, cookie: Cookie, refusjon: Refusjon?, status: ResultMatcher = status().isOk): String {
        if (refusjon != null) {
            request.content(mapper.writeValueAsString(refusjon))
        }

        return mockMvc.perform(
                request
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .cookie(cookie))
                .andExpect(status)
                .andReturn()
                .response.getContentAsString(StandardCharsets.UTF_8)
    }

    private fun sendRequest(request: MockHttpServletRequestBuilder, cookie: Cookie, forventetStatus: ResultMatcher) {
        mockMvc.perform(
                request
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .cookie(cookie))
                .andExpect(forventetStatus)
    }

    private final fun lagTokenForFnr(fnr: String): String? {
        val now = Date()
        val claims = JWTClaimsSet.Builder()
                .subject(UUID.randomUUID().toString())
                .issuer("tokenx")
                .audience("aud-localhost")
                .jwtID(UUID.randomUUID().toString())
                .claim("pid", fnr)
                .claim("acr", "Level4")
                .claim("ver", "1.0")
                .claim("nonce", "myNonce")
                .claim("auth_time", now)
                .notBeforeTime(now)
                .issueTime(now)
                .expirationTime(Date(now.time + 1000000)).build()

        return JwtTokenGenerator.createSignedJWT(JwkGenerator.getDefaultRSAKey(), claims).serialize()
    }

    private final fun lagTokenForNavId(navId: String): String? {
        val now = Date()
        val claims = JWTClaimsSet.Builder()
                .subject(UUID.randomUUID().toString())
                .claim("NAVident", navId)
                .issuer("aad")
                .audience("aud-localhost")
                .jwtID(UUID.randomUUID().toString())
                .claim("ver", "1.0")
                .claim("auth_time", now)
                .claim("nonce", "myNonce")
                .notBeforeTime(now)
                .issueTime(now)
                .expirationTime(Date(now.time + 1000000)).build()

        return JwtTokenGenerator.createSignedJWT(JwkGenerator.getDefaultRSAKey(), claims).serialize()
    }
}
