package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.jayway.jsonpath.JsonPath
import com.ninjasquad.springmockk.SpykBean
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.verify
import no.nav.arbeidsgiver.tiltakrefusjon.Feilkode
import no.nav.arbeidsgiver.tiltakrefusjon.altinn.Organisasjon
import no.nav.arbeidsgiver.tiltakrefusjon.audit.AuditConsoleLogger
import no.nav.arbeidsgiver.tiltakrefusjon.autorisering.ADMIN_BRUKER
import no.nav.arbeidsgiver.tiltakrefusjon.autorisering.REQUEST_MAPPING_INNLOGGET_ARBEIDSGIVER
import no.nav.arbeidsgiver.tiltakrefusjon.hendelseslogg.HendelsesloggRepository
import no.nav.arbeidsgiver.tiltakrefusjon.refusjoner
import no.nav.arbeidsgiver.tiltakrefusjon.utils.Now
import no.nav.arbeidsgiver.tiltakrefusjon.varsling.VarslingRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock
import org.springframework.http.MediaType
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultMatcher
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse.BodyHandlers
import java.nio.charset.StandardCharsets

data class InnloggetBrukerTest(val identifikator: String, val organisasjoner: Set<Organisasjon>)


@SpringBootTest
@ActiveProfiles("local")
@AutoConfigureMockMvc
@AutoConfigureWireMock
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DirtiesContext
class RefusjonApiTest(
    @param:Autowired val refusjonRepository: RefusjonRepository,
    @param:Autowired val refusjonService: RefusjonService,
    @param:Autowired val mapper: ObjectMapper,
    @param:Autowired val mockMvc: MockMvc,
    @param:Autowired val hendelsesloggRepository: HendelsesloggRepository,
    @param:Autowired val fristForlengetRepository: FristForlengetRepository,
    @param:Autowired val korreksjonRepository: KorreksjonRepository,
    @param:Autowired val varslingRepository: VarslingRepository
) {
    @SpykBean
    lateinit var consoleLogger: AuditConsoleLogger

    val navToken = lagTokenForNavId("Z123456", "550e8400-e29b-41d4-a716-446655440000")
    val arbGiverToken = lagTokenForFnr("16120102137")

    @BeforeEach
    fun setUp() {
        refusjonRepository.saveAll(refusjoner())
        refusjonRepository.findAll().forEach {
            refusjonService.oppdaterRefusjon(it, ADMIN_BRUKER)
        }
        resetAuditCount()
    }

    private fun resetAuditCount() {
        clearMocks(consoleLogger)
        every {
            consoleLogger.logg(any())
        } returns Unit
    }

    @AfterEach
    fun tearDown() {
        varslingRepository.deleteAll()
        fristForlengetRepository.deleteAll()
        hendelsesloggRepository.deleteAll()
        korreksjonRepository.deleteAll()
        refusjonRepository.deleteAll()
    }

    @Test
    fun `hentAlle() er tilgjengelig for saksbehandler`() {
        val json = sendRequest(get("$REQUEST_MAPPING_SAKSBEHANDLER_REFUSJON?enhet=1000"), navToken)
        val refusjoner: List<Any> = JsonPath.read(json, "$.refusjoner")
        assertFalse(refusjoner.isEmpty())
    }

    @Test
    fun `hentAlle() er utilgjengelig for arbeidsgiver`() {
        mockMvc.perform(
            get(REQUEST_MAPPING_ARBEIDSGIVER_REFUSJON)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .header("authorization", "Bearer $arbGiverToken")
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `hentAlleMedBedriftnummer() - Arbeidsgiver henter refusjoner for en bedrift`() {
        // GITT
        val bedriftNr = "998877665"

        // NÅR
        val json = sendRequest(get("$REQUEST_MAPPING_ARBEIDSGIVER_REFUSJON?bedriftNr=$bedriftNr"), arbGiverToken)
        val bedriftNrListe: List<String> = JsonPath.read(json, "$[*].bedriftNr")

        // DA
        assertEquals(List(4) { bedriftNr }, bedriftNrListe)
    }


    @Test
    fun `hentAlle refusjon for alle bedrifter arbeidsgiver har tilgang til`() {
        // GITT
        val BEDRIFT_NR1 = "998877665"
        val BEDRIFT_NR2 = "999999999"

        // NÅR
        val brukerJson = sendRequest(get("$REQUEST_MAPPING_INNLOGGET_ARBEIDSGIVER/innlogget-bruker"), arbGiverToken)
        val bruker: InnloggetBrukerTest = mapper.readValue(brukerJson, object : TypeReference<InnloggetBrukerTest>() {})
        val orgNr = bruker.organisasjoner.map { it.organizationNumber }

        val page1Json =
            sendRequest(get("$REQUEST_MAPPING_ARBEIDSGIVER_REFUSJON/hentliste?page=0&size=3"), arbGiverToken)
        val page2Json =
            sendRequest(get("$REQUEST_MAPPING_ARBEIDSGIVER_REFUSJON/hentliste?page=1&size=3"), arbGiverToken)
        val page3Json =
            sendRequest(get("$REQUEST_MAPPING_ARBEIDSGIVER_REFUSJON/hentliste?page=0&size=6"), arbGiverToken)

        val bedrifter1: List<String> = JsonPath.read(page1Json, "$.refusjoner[*].bedriftNr")
        val bedrifter2: List<String> = JsonPath.read(page2Json, "$.refusjoner[*].bedriftNr")
        val bedrifter3: List<String> = JsonPath.read(page3Json, "$.refusjoner[*].bedriftNr")

        // SÅ
        assertTrue(bedrifter1.all { it in orgNr })
        assertTrue(bedrifter2.all { it in orgNr })
        assertTrue(bedrifter3.all { it in orgNr })

        assertThat(JsonPath.read<Int>(page1Json, "$.size")).isEqualTo(3)
        assertThat(JsonPath.read<Int>(page2Json, "$.size")).isEqualTo(3)
        assertThat(JsonPath.read<Int>(page3Json, "$.size")).isEqualTo(6)

        val ids1: List<String> = JsonPath.read(page1Json, "$.refusjoner[*].id")
        val ids2: List<String> = JsonPath.read(page2Json, "$.refusjoner[*].id")
        val ids3: List<String> = JsonPath.read(page3Json, "$.refusjoner[*].id")
        assertThat(ids3).contains(ids1[0])
        assertThat(ids3).contains(ids2[0])

        // NÅR
        val json4 = sendRequest(
            get("$REQUEST_MAPPING_ARBEIDSGIVER_REFUSJON/hentliste?bedriftNr=$BEDRIFT_NR1,$BEDRIFT_NR2&page=0&size=6"),
            arbGiverToken
        )
        val bedrifter4: List<String> = JsonPath.read(json4, "$.refusjoner[*].bedriftNr")

        // SÅ
        assertTrue(bedrifter4.all { it in orgNr })
        assertTrue(bedrifter4.all { it == BEDRIFT_NR1 || it == BEDRIFT_NR2 })
    }

    @Test
    fun `hent() - Arbeidsgiver henter refusjon med id`() {
        val id = refusjonRepository.findAll().find { it.deltakerFnr == "07098142678" }?.id

        val json = sendRequest(get("$REQUEST_MAPPING_ARBEIDSGIVER_REFUSJON/$id"), arbGiverToken)
        assertEquals(id, JsonPath.read<String>(json, "$.id"))

        verify(exactly = 1) {
            consoleLogger.logg(any())
        }
    }

    @Test
    fun `hent() - Arbeidsgiver mangler tilgang til refusjon med id`() {
        val id = refusjonRepository.findAll().find { it.deltakerFnr == "23119409195" }?.id

        sendRequest(get("$REQUEST_MAPPING_ARBEIDSGIVER_REFUSJON/$id"), arbGiverToken, status().isForbidden)
    }

    @Test
    fun `hent() - Saksbehandler henter refusjon med id`() {
        val id = refusjonRepository.findAll().find { it.deltakerFnr == "28128521498" }?.id

        val json = sendRequest(get("$REQUEST_MAPPING_SAKSBEHANDLER_REFUSJON/$id"), navToken)
        assertEquals(id, JsonPath.read<String>(json, "$.id"))

        verify(exactly = 1) {
            consoleLogger.logg(any())
        }
    }

    @Test
    fun `hent() - refusjon-payload inneholder ikke deltakerFnr pa noe niva`() {
        val id = refusjonRepository.findAll().find { it.deltakerFnr == "28128521498" }?.id

        val json = sendRequest(get("$REQUEST_MAPPING_ARBEIDSGIVER_REFUSJON/$id"), arbGiverToken)

        assertTrue(mapper.readTree(json).findValues("deltakerFnr").isEmpty())
    }

    @Test
    fun `hent() - Saksbehandler mangler tilgang til henter refusjon med id`() {
        val id = refusjonRepository.findAll().find { it.deltakerFnr == "07098142678" }?.id
        sendRequest(get("$REQUEST_MAPPING_SAKSBEHANDLER_REFUSJON/$id"), navToken, status().isForbidden)
    }

    @Test
    fun `Får feil hvis cookie mangler arbeidsgiver`() {
        mockMvc.perform(
            get(REQUEST_MAPPING_ARBEIDSGIVER_REFUSJON)
        )
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `Får feil hvis cookie mangler saksbehandler`() {
        mockMvc.perform(
            get(REQUEST_MAPPING_SAKSBEHANDLER_REFUSJON)
        )
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `Arbeidsgiver kan gjøre inntektsoppslag, og hente refusjon med inntektsgrunnlag, og godkjenne`() {
        val id = refusjonRepository.findAll().find { it.deltakerFnr == "28128521498" }?.id

        // Inntektsoppslag ved henting av refusjon
        oppdaterRefusjonMedKontonrOgInntekter(id!!)
        val refusjonEtterInntektsgrunnlag = hentRefusjon(id)
        assertThat(
            JsonPath.read<Any?>(
                refusjonEtterInntektsgrunnlag,
                "$.refusjonsgrunnlag.inntektsgrunnlag"
            )
        ).isNotNull()

        // Huker av for at inntektene er opptjent i periode
        val inntektsgrunnlagJson = mapper.writeValueAsString(
            JsonPath.read(refusjonEtterInntektsgrunnlag, "$.refusjonsgrunnlag.inntektsgrunnlag")
        )
        val inntektsgrunnlag = mapper.readValue(inntektsgrunnlagJson, Inntektsgrunnlag::class.java)
        inntektsgrunnlag.inntekter.filter { it.erMedIInntektsgrunnlag() }.forEach {
            setInntektslinjeOpptjentIPeriode(
                refusjonId = id,
                inntektslinjeId = it.id,
                erOpptjentIPeriode = true
            )
        }

        // Svarer på spørsmål om alle inntekter er fra tiltaket
        sendRequest(
            post("$REQUEST_MAPPING_ARBEIDSGIVER_REFUSJON/$id/endre-bruttolønn"),
            arbGiverToken,
            EndreBruttolønnRequest(true, null)
        )
        val refusjonEtterInntektssporsmal = hentRefusjon(id)
        assertThat(
            JsonPath.read<Int>(
                refusjonEtterInntektssporsmal,
                "$.refusjonsgrunnlag.beregning.refusjonsbeløp"
            )
        ).isPositive()
        val harLagretHendelselogg = hendelsesloggRepository.findAll()
            .find { it.refusjonId == id && it.event == "BeregningUtført" && it.appImageId != null } != null
        assertTrue(harLagretHendelselogg)

        // Godkjenn
        sendRequest(post("$REQUEST_MAPPING_ARBEIDSGIVER_REFUSJON/$id/godkjenn"), arbGiverToken)
        val refusjonEtterGodkjennelse = hentRefusjon(id)
        assertThat(JsonPath.read<String?>(refusjonEtterGodkjennelse, "$.godkjentAvArbeidsgiver")).isNotNull()
    }

    @Test
    fun `feilkode setter riktig header og gir statuskode 400`() {
        val id = refusjonRepository.findAll().find { it.deltakerFnr == "28128521498" }?.id

        // Godkjenn
        mockMvc.perform(
            post("$REQUEST_MAPPING_ARBEIDSGIVER_REFUSJON/$id/godkjenn")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .header("If-Unmodified-Since", Now.instant())
                .header("authorization", "Bearer $arbGiverToken")
        )
            .andExpect(status().isBadRequest)
            .andExpect(header().string("feilkode", Feilkode.INGEN_INNTEKTER.toString()))
    }

    private fun setInntektslinjeOpptjentIPeriode(
        refusjonId: String,
        inntektslinjeId: String,
        erOpptjentIPeriode: Boolean
    ) {
        sendRequest(
            post("$REQUEST_MAPPING_ARBEIDSGIVER_REFUSJON/$refusjonId/set-inntektslinje-opptjent-i-periode"),
            arbGiverToken,
            EndreRefundertInntektslinjeRequest(inntektslinjeId, erOpptjentIPeriode)
        )
    }

    private fun hentRefusjon(id: String?): String {
        return sendRequest(get("$REQUEST_MAPPING_ARBEIDSGIVER_REFUSJON/$id"), arbGiverToken)
    }

    private fun oppdaterRefusjonMedKontonrOgInntekter(id: String) {
        sendRequest(post("$REQUEST_MAPPING_ARBEIDSGIVER_REFUSJON/$id/sett-kontonummer-og-inntekter"), arbGiverToken)
    }

    private fun sendRequest(request: MockHttpServletRequestBuilder, token: String): String {
        return sendRequest(request, token, null)
    }

    private fun sendRequest(
        request: MockHttpServletRequestBuilder,
        token: String,
        content: Any?,
        status: ResultMatcher = status().isOk
    ): String {
        if (content != null) {
            request.content(mapper.writeValueAsString(content)).contentType(MediaType.APPLICATION_JSON)
        }

        return mockMvc.perform(
            request
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .header("If-Unmodified-Since", Now.instant())
                .header("authorization", "Bearer $token")
        )
            .andExpect(status)
            .andReturn()
            .response.getContentAsString(StandardCharsets.UTF_8)
    }

    private fun sendRequest(request: MockHttpServletRequestBuilder, token: String, forventetStatus: ResultMatcher) {
        mockMvc.perform(
            request
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .header("If-Unmodified-Since", Now.instant())
                .header("authorization", "Bearer $token")
        )
            .andExpect(forventetStatus)
    }

    private final fun lagTokenForFnr(fnr: String): String {
        return HttpClient.newHttpClient().send(
            HttpRequest.newBuilder()
                .GET()
                .uri(URI.create("https://tiltak-fakelogin.ekstern.dev.nav.no/token?pid=${fnr}&aud=aud-tokenx&iss=tokenx&acr=Level4"))
                .build(), BodyHandlers.ofString()
        ).body()
    }

    private final fun lagTokenForNavId(navId: String, azureId: String): String {
        return HttpClient.newHttpClient().send(
            HttpRequest.newBuilder()
                .GET()
                .uri(URI.create("https://tiltak-fakelogin.ekstern.dev.nav.no/token?NAVident=${navId}&iss=aad&aud=aud-aad&oid=${azureId}"))
                .build(), BodyHandlers.ofString()
        ).body()
    }
}
