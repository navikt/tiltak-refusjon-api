package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.nimbusds.jwt.JWTClaimsSet
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.nydatamodell.Refusjonsak
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.nydatamodell.RefusjonsakRepository
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.nio.charset.StandardCharsets
import java.util.*
import javax.servlet.http.Cookie


@SpringBootTest
@ActiveProfiles("local")
@AutoConfigureMockMvc
@AutoConfigureWireMock(port = 8090)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RefusjonsakApiTest(
        @Autowired val refusjonsakRepository: RefusjonsakRepository,
        @Autowired val mapper: ObjectMapper,
        @Autowired val mockMvc: MockMvc
) {

    private final val TOKEN_X_COOKIE_NAVN = "tokenx-token"
    private final val AAD_COOKIE_NAVN = "aad-token"

    val navCookie = Cookie(AAD_COOKIE_NAVN, lagTokenForNavId("Z123456"))
    val arbGiverCookie = Cookie(TOKEN_X_COOKIE_NAVN, lagTokenForFnr("16120102137"))

    @BeforeEach
    fun setUp() {
        refusjonsakRepository.deleteAll()
        refusjonsakRepository.saveAll(refusjoner())
    }

//    @Test
//    fun `hentBeregnetRefusjonsak() for deltaker, bedrift og periode når response inneholder ikke inntekter fra inntektskomponenten`(){
//        // GITT
//        val bedriftNr = "998877000"
//        val deltakerFnr = "00128521000"
//        val datoRefusjonsakPeriodeFom ="2020-09-01"
//        val datoRefusjonsakPeriodeTom = "2020-10-01"
//        val refusjonsberegningRequest = RefusjonsaksberegningRequest(deltakerFnr, bedriftNr, datoRefusjonsakPeriodeFom, datoRefusjonsakPeriodeTom)
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
//    fun `hentBeregnetRefusjonsak() for deltaker, bedrift og periode når request  periode er ikke helt utfylt`(){
//        // GITT
//        val bedriftNr = "998877000"
//        val deltakerFnr = "28128521000"
//        val datoRefusjonsakPeriodeFom ="aaaa"
//        val datoRefusjonsakPeriodeTom = "asdasds"
//        val refusjonsberegningRequest = RefusjonsaksberegningRequest(deltakerFnr, bedriftNr, datoRefusjonsakPeriodeFom, datoRefusjonsakPeriodeTom)
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
//    fun `hentBeregnetRefusjonsak() for deltaker, bedrift og periode når request er ikke helt utfylt`(){
//        // GITT
//        val bedriftNr = "    "
//        val deltakerFnr = "   "
//        val datoRefusjonsakPeriodeFom ="2020-09-01"
//        val datoRefusjonsakPeriodeTom = "2020-10-01"
//        val refusjonsberegningRequest = RefusjonsaksberegningRequest(deltakerFnr, bedriftNr, datoRefusjonsakPeriodeFom, datoRefusjonsakPeriodeTom)
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
//    fun `hentBeregnetRefusjonsak() for deltaker, bedrift og periode når request ident inneholder ugyldig tegn`(){
//        // GITT
//        val bedriftNr = "998877665"
//        val deltakerFnr = "2812aaaaa8521498"
//        val datoRefusjonsakPeriodeFom ="2020-09-01"
//        val datoRefusjonsakPeriodeTom = "2020-10-01"
//        val refusjonsberegningRequest = RefusjonsaksberegningRequest(deltakerFnr, bedriftNr, datoRefusjonsakPeriodeFom, datoRefusjonsakPeriodeTom)
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
//    fun `hentBeregnetRefusjonsak() for deltaker, bedrift og periode hvor de ikke finnes`(){
//        // GITT
//        val bedriftNr = "998877000"
//        val deltakerFnr = "28128521000"
//        val datoRefusjonsakPeriodeFom ="2012-09-01"
//        val datoRefusjonsakPeriodeTom = "2012-10-01"
//        val refusjonsberegningRequest = RefusjonsaksberegningRequest(deltakerFnr, bedriftNr, datoRefusjonsakPeriodeFom, datoRefusjonsakPeriodeTom)
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
//    fun `hentBeregnetRefusjonsak() for deltaker, bedrift og periode`(){
//        // GITT
//        val bedriftNr = "998877665"
//        val deltakerFnr = "28128521498"
//        val datoRefusjonsakPeriodeFom ="2020-09-01"
//        val datoRefusjonsakPeriodeTom = "2020-10-01"
//        val refusjonsberegningRequest = RefusjonsaksberegningRequest(deltakerFnr, bedriftNr, datoRefusjonsakPeriodeFom, datoRefusjonsakPeriodeTom)
//
//        // NÅR
//        val request = post("$REQUEST_MAPPING/beregn")
//                .content( ObjectMapper().writeValueAsString(refusjonsberegningRequest))
//                .contentType(MediaType.APPLICATION_JSON)
//                .accept(MediaType.APPLICATION_JSON)
//
//        val json = sendRequest(request, arbGiverCookie)
//        val refusjonsgrunnlag = mapper.readValue(json, Refusjonsaksberegner::class.java)
//
//        // SÅ
//        assertNotNull(refusjonsgrunnlag!!)
//        assertEquals(3, refusjonsgrunnlag.inntekter.size)
//        assertEquals(refusjonsgrunnlag.beløp,11245)
//    }


    @Test
    fun `hentRefusjonsak() for deltaker, bedrift og periode som ikke finnes`(){
        // GITT
        val bedriftNr = "998877665"
        val deltakerFnr = "28128521498"
        val datoRefusjonsakPeriodeFom = "2000-09-01"
        val datoRefusjonsakPeriodeTom = "2000-10-01"

        // NÅR
        val json = sendRequest(get("$REQUEST_MAPPING/deltaker/$deltakerFnr/bedrift/$bedriftNr/fra/$datoRefusjonsakPeriodeFom/til/$datoRefusjonsakPeriodeTom"), arbGiverCookie)


        // SÅ
        assertTrue(json.isBlank())
    }


    @Test
    fun `hentRefusjonsak() for deltaker, bedrift og periode`(){
        // GITT
        val bedriftNr = "998877665"
        val deltakerFnr = "28128521498"
        val datoRefusjonsakPeriodeFom = "2020-09-01"
        val datoRefusjonsakPeriodeTom = "2020-10-01"

        // NÅR
        val json = sendRequest(get("$REQUEST_MAPPING/deltaker/$deltakerFnr/bedrift/$bedriftNr/fra/$datoRefusjonsakPeriodeFom/til/$datoRefusjonsakPeriodeTom"), arbGiverCookie)
        val refusjon = mapper.readValue(json, Refusjonsak::class.java)

        // SÅ
        assertNotNull(refusjon)
        assertNotNull(refusjon?.deltakerFnr.equals("28128521498")
                && refusjon?.bedriftNr.equals("998877665"))
    }


    @Test
    fun `hentAlle() er tilgjengelig for saksbehandler`() {
        val json = sendRequest(get(REQUEST_MAPPING), navCookie)
        val liste = mapper.readValue(json, object : TypeReference<List<Refusjonsak?>?>() {})
        assertFalse(liste!!.isEmpty())
    }

    @Test
    fun `hentAlle() - Saksbehandler har ikke leserettighet til en refusjon`() {
        val json = sendRequest(get(REQUEST_MAPPING), navCookie)
        val liste = mapper.readValue(json, object : TypeReference<List<Refusjonsak?>?>() {})

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
        val bedriftNr = "998877665"

        // NÅR
        val json = sendRequest(get("$REQUEST_MAPPING/bedrift/$bedriftNr"), arbGiverCookie)
        val liste = mapper.readValue(json, object : TypeReference<List<Refusjonsak?>?>() {})

        // DA
        assertTrue(liste!!.all { it!!.bedriftNr.equals(bedriftNr) })
        assertEquals(4, liste.size)
    }

    @Test
    fun `hentAlleMedBedriftnummer() - Saksbehandler henter refusjoner for en bedrift`() {
        // GITT
        val bedriftNr = "998877665"

        // NÅR
        val json = sendRequest(get("$REQUEST_MAPPING/bedrift/$bedriftNr"), navCookie)
        val liste = mapper.readValue(json, object : TypeReference<List<Refusjonsak?>?>() {})

        // SÅ
        assertTrue(liste!!.all { it!!.bedriftNr.equals(bedriftNr) })
        assertEquals(4, liste.size)
    }

    @Test
    fun `hentAlleMedBedriftnummer() - skal ikke kunne hente refusjoner for en person som saksbehandler ikke har tilgang til`() {
        // GITT
        val fnrForPerson = "07098142678"
        val bedriftNr = "999999999"

        // NÅR
        val json = sendRequest(get("$REQUEST_MAPPING/bedrift/$bedriftNr"), navCookie)
        val liste = mapper.readValue(json, object : TypeReference<List<Refusjonsak?>?>() {})
        assertNull(liste?.find { refusjon -> refusjon?.deltakerFnr.equals(fnrForPerson) })
    }


    @Test
    fun `hent() - Arbeidsgiver henter refusjon med id`() {
        val id = "2"
        val json = sendRequest(get("$REQUEST_MAPPING/$idTilRefusjonAgIkkeHarTilgangTil"), arbGiverCookie)
        val refusjon = mapper.readValue(json, Refusjonsak::class.java)
        assertEquals(id, refusjon.id)
    }

    @Test
    fun `hent() - Arbeidsgiver mangler tilgang til refusjon med id`() {
        val idTilRefusjonAgIkkeHarTilgangTil = refusjonsakRepository.findAll().firstOrNull{it.bedriftNr != ...}.id

        sendRequest(get("$REQUEST_MAPPING/$idTilRefusjonAgIkkeHarTilgangTil"), arbGiverCookie, status().isUnauthorized)
    }

    @Test
    fun `hent() - Saksbehandler henter refusjon med id`() {
        val id = "2"
        val json = sendRequest(get("$REQUEST_MAPPING/$id"), navCookie)
        val refusjon = mapper.readValue(json, Refusjonsak::class.java)
        assertEquals(id, refusjon.id)
    }

    @Test
    fun `hent() - Saksbehandler mangler tilgang til henter refusjon med id`() {
        val id = "1"
        sendRequest(get("$REQUEST_MAPPING/$id"), navCookie, status().isUnauthorized)
    }

    @Test
    fun `Får feil hvis cookie mangler`() {
        mockMvc.perform(
                get(REQUEST_MAPPING))
                .andExpect(status().isUnauthorized)
    }

    private fun sendRequest(request: MockHttpServletRequestBuilder, cookie: Cookie): String {
        return sendRequest(request, cookie, null)
    }

    private fun sendRequest(request: MockHttpServletRequestBuilder, cookie: Cookie, refusjon: Refusjonsak?, status: ResultMatcher = status().isOk): String {

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
