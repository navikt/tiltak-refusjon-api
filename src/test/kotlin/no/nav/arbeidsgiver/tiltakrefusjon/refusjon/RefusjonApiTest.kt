package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.nimbusds.jwt.JWTClaimsSet
import no.nav.arbeidsgiver.tiltakrefusjon.refusjoner
import no.nav.security.token.support.test.JwkGenerator
import no.nav.security.token.support.test.JwtTokenGenerator
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
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
        refusjonRepository.deleteAll()
        refusjonRepository.saveAll(refusjoner())
    }

//    @Test
//    fun `hentBeregnetRefusjon() for deltaker, bedrift og periode når response inneholder ikke inntekter fra inntektskomponenten`(){
//        // GITT
//        val bedriftNr = "998877000"
//        val deltakerFnr = "00128521000"
//        val datoRefusjonPeriodeFom ="2020-09-01"
//        val datoRefusjonPeriodeTom = "2020-10-01"
//        val refusjonsberegningRequest = RefusjonsberegningRequest(deltakerFnr, bedriftNr, datoRefusjonPeriodeFom, datoRefusjonPeriodeTom)
//
//        // NÅR
//        val request = post("$REQUEST_MAPPING/beregn")
//                .content( ObjectMapper().writeValueAsString(refusjonsberegningRequest))
//                .contentType(MediaType.APPLICATION_JSON)
//                .accept(MediaType.APPLICATION_JSON)
//                .cookie(arbGiverCookie)
//
//        mockMvc.perform(request)
//                .andExpect(status().isServiceUnavailable)
//    }
//
//
//    @Test
//    fun `hentBeregnetRefusjon() for deltaker, bedrift og periode når request  periode er ikke helt utfylt`(){
//        // GITT
//        val bedriftNr = "998877000"
//        val deltakerFnr = "28128521000"
//        val datoRefusjonPeriodeFom ="aaaa"
//        val datoRefusjonPeriodeTom = "asdasds"
//        val refusjonsberegningRequest = RefusjonsberegningRequest(deltakerFnr, bedriftNr, datoRefusjonPeriodeFom, datoRefusjonPeriodeTom)
//
//        // NÅR
//        val request = post("$REQUEST_MAPPING/beregn")
//                .content( ObjectMapper().writeValueAsString(refusjonsberegningRequest))
//                .contentType(MediaType.APPLICATION_JSON)
//                .accept(MediaType.APPLICATION_JSON)
//                .cookie(arbGiverCookie)
//
//        mockMvc.perform(request)
//                .andExpect(status().isServiceUnavailable)
//    }
//
//
//    @Test
//    fun `hentBeregnetRefusjon() for deltaker, bedrift og periode når request er ikke helt utfylt`(){
//        // GITT
//        val bedriftNr = "    "
//        val deltakerFnr = "   "
//        val datoRefusjonPeriodeFom ="2020-09-01"
//        val datoRefusjonPeriodeTom = "2020-10-01"
//        val refusjonsberegningRequest = RefusjonsberegningRequest(deltakerFnr, bedriftNr, datoRefusjonPeriodeFom, datoRefusjonPeriodeTom)
//
//        // NÅR
//        val request = post("$REQUEST_MAPPING/beregn")
//                .content( ObjectMapper().writeValueAsString(refusjonsberegningRequest))
//                .contentType(MediaType.APPLICATION_JSON)
//                .accept(MediaType.APPLICATION_JSON)
//                .cookie(arbGiverCookie)
//
//        mockMvc.perform(request)
//                .andExpect(status().isServiceUnavailable)
//    }
//
//    @Test
//    fun `hentBeregnetRefusjon() for deltaker, bedrift og periode når request ident inneholder ugyldig tegn`(){
//        // GITT
//        val bedriftNr = "998877665"
//        val deltakerFnr = "2812aaaaa8521498"
//        val datoRefusjonPeriodeFom ="2020-09-01"
//        val datoRefusjonPeriodeTom = "2020-10-01"
//        val refusjonsberegningRequest = RefusjonsberegningRequest(deltakerFnr, bedriftNr, datoRefusjonPeriodeFom, datoRefusjonPeriodeTom)
//
//        // NÅR
//        val request = post("$REQUEST_MAPPING/beregn")
//                .content( ObjectMapper().writeValueAsString(refusjonsberegningRequest))
//                .contentType(MediaType.APPLICATION_JSON)
//                .accept(MediaType.APPLICATION_JSON)
//                .cookie(arbGiverCookie)
//
//        mockMvc.perform(request)
//                .andExpect(status().isServiceUnavailable)
//    }
//
//    @Test
//    fun `hentBeregnetRefusjon() for deltaker, bedrift og periode hvor de ikke finnes`(){
//        // GITT
//        val bedriftNr = "998877000"
//        val deltakerFnr = "28128521000"
//        val datoRefusjonPeriodeFom ="2012-09-01"
//        val datoRefusjonPeriodeTom = "2012-10-01"
//        val refusjonsberegningRequest = RefusjonsberegningRequest(deltakerFnr, bedriftNr, datoRefusjonPeriodeFom, datoRefusjonPeriodeTom)
//
//        // NÅR
//        val request = post("$REQUEST_MAPPING/beregn")
//                .content( ObjectMapper().writeValueAsString(refusjonsberegningRequest))
//                .contentType(MediaType.APPLICATION_JSON)
//                .accept(MediaType.APPLICATION_JSON)
//                .cookie(arbGiverCookie)
//
//        // SÅ
//        mockMvc.perform(request)
//                .andExpect(status().isServiceUnavailable)
//
//
//    }
//
//    @Test
//    fun `hentBeregnetRefusjon() for deltaker, bedrift og periode`(){
//        // GITT
//        val bedriftNr = "998877665"
//        val deltakerFnr = "28128521498"
//        val datoRefusjonPeriodeFom ="2020-09-01"
//        val datoRefusjonPeriodeTom = "2020-10-01"
//        val refusjonsberegningRequest = RefusjonsberegningRequest(deltakerFnr, bedriftNr, datoRefusjonPeriodeFom, datoRefusjonPeriodeTom)
//
//        // NÅR
//        val request = post("$REQUEST_MAPPING/beregn")
//                .content( ObjectMapper().writeValueAsString(refusjonsberegningRequest))
//                .contentType(MediaType.APPLICATION_JSON)
//                .accept(MediaType.APPLICATION_JSON)
//
//        val json = sendRequest(request, arbGiverCookie)
//        val refusjonsgrunnlag = mapper.readValue(json, Refusjonsberegner::class.java)
//
//        // SÅ
//        assertNotNull(refusjonsgrunnlag!!)
//        assertEquals(3, refusjonsgrunnlag.inntekter.size)
//        assertEquals(refusjonsgrunnlag.beløp,11245)
//    }

    @Test
    fun `hentAlle() er tilgjengelig for saksbehandler`() {
        val json = sendRequest(get(REQUEST_MAPPING_SAKSBEHANDLER_REFUSJON), navCookie)
        val liste = mapper.readValue(json, object : TypeReference<List<Refusjon?>?>() {})
        assertFalse(liste!!.isEmpty())
    }

    @Test
    fun `hentAlle() - Saksbehandler har ikke leserettighet til en refusjon`() {
        val json = sendRequest(get(REQUEST_MAPPING_SAKSBEHANDLER_REFUSJON), navCookie)
        val liste = mapper.readValue(json, object : TypeReference<List<Refusjon>>() {})

        assertEquals(14, liste.size)
        assertNull(liste.find { it.deltakerFnr == "07098142678" })
    }

    @Test
    fun `hentAlle() er utilgjengelig for arbeidsgiver`() {
        mockMvc.perform(get(REQUEST_MAPPING_ARBEIDSGIVER_REFUSJON).contentType(MediaType.APPLICATION_JSON)
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
        assertEquals(4, liste.size)
    }

    @Test
    fun `hentAlleMedBedriftnummer() - Saksbehandler henter refusjoner for en bedrift`() {
        // GITT
        val bedriftNr = "998877665"

        // NÅR
        val json = sendRequest(get("$REQUEST_MAPPING_SAKSBEHANDLER_REFUSJON?bedriftNr=$bedriftNr"), navCookie)
        val liste = mapper.readValue(json, object : TypeReference<List<Refusjon>>() {})

        // SÅ
        assertTrue(liste.all { it.bedriftNr == bedriftNr })
        assertEquals(4, liste.size)
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
        val id = refusjonRepository.findAll().find { it.deltakerFnr == "07098142678" }?.id

        sendRequest(get("$REQUEST_MAPPING_ARBEIDSGIVER_REFUSJON/$id"), arbGiverCookie, status().isUnauthorized)
    }

    @Test
    fun `hent() - Saksbehandler henter refusjon med id`() {
        val id = refusjonRepository.findAll().find { it.deltakerFnr == "07098142678" }?.id

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
