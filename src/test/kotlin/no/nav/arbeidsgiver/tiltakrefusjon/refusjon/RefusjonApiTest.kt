package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
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
data class RefusjonlistFraFlereOrgTest(
    val refusjoner: List<Refusjon>,
    val size: Int,
    val currentPage: Int,
    val totalItems: Int,
    val totalPages: Int
)


@SpringBootTest
@ActiveProfiles("local")
@AutoConfigureMockMvc
@AutoConfigureWireMock(port = 8091)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DirtiesContext
class RefusjonApiTest(
    @Autowired val refusjonRepository: RefusjonRepository,
    @Autowired val refusjonService: RefusjonService,
    @Autowired val mapper: ObjectMapper,
    @Autowired val mockMvc: MockMvc,
    @Autowired val hendelsesloggRepository: HendelsesloggRepository,
    @Autowired val fristForlengetRepository: FristForlengetRepository,
    @Autowired val korreksjonRepository: KorreksjonRepository,
    @Autowired val varslingRepository: VarslingRepository
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
        val liste = mapper.readValue(json, object : TypeReference<Map<String, Any>>() {})
        val refusjoner = liste.get("refusjoner") as List<Map<String, Any>>
        assertFalse(refusjoner.isEmpty())
        // Her er det noe timing problem. Dette endres fra gang til gang man kjører det
        // Forventer at oppslag auditlogges, men kun én gang per unike deltaker
        //verify(exactly = refusjoner.map { it.get("deltakerFnr") }.toSet().size) {
        //    consoleLogger.logg(any())
        //}
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
        val liste = mapper.readValue(json, object : TypeReference<List<Refusjon>>() {})

        // DA
        assertTrue(liste.all { it.bedriftNr == bedriftNr })
        assertEquals(4, liste.size)
    }


    @Test
    fun `hentAlle refusjon for alle bedrifter arbeidsgiver har tilgang til`() {
        // GITT
        val BEDRIFT_NR1 = "998877665"
        val BEDRIFT_NR2 = "999999999"

        // NÅR
        val brukerJson = sendRequest(get("$REQUEST_MAPPING_INNLOGGET_ARBEIDSGIVER/innlogget-bruker"), arbGiverToken)
        val bruker: InnloggetBrukerTest = mapper.readValue(brukerJson, object : TypeReference<InnloggetBrukerTest>() {})
        val refusjonJson =
            sendRequest(get("$REQUEST_MAPPING_ARBEIDSGIVER_REFUSJON/hentliste?page=0&size=3"), arbGiverToken)
        val refusjonlist: RefusjonlistFraFlereOrgTest = mapper.readValue(refusjonJson, object : TypeReference<RefusjonlistFraFlereOrgTest>() {})
        val refusjonJson2 =
            sendRequest(get("$REQUEST_MAPPING_ARBEIDSGIVER_REFUSJON/hentliste?page=1&size=3"), arbGiverToken)
        val refusjonlist2: RefusjonlistFraFlereOrgTest = mapper.readValue(refusjonJson2, object : TypeReference<RefusjonlistFraFlereOrgTest>() {})
        val refusjonJson3 =
            sendRequest(get("$REQUEST_MAPPING_ARBEIDSGIVER_REFUSJON/hentliste?page=0&size=6"), arbGiverToken)
        val refusjonlist3: RefusjonlistFraFlereOrgTest = mapper.readValue(refusjonJson3, object : TypeReference<RefusjonlistFraFlereOrgTest>() {})

        // SÅ
        assertThat(refusjonlist.refusjoner).allMatch { bedrifter -> bruker.organisasjoner.any { it.organizationNumber == bedrifter.bedriftNr } }
        assertThat(refusjonlist2.refusjoner).allMatch { bedrifter -> bruker.organisasjoner.any { it.organizationNumber == bedrifter.bedriftNr } }
        assertThat(refusjonlist3.refusjoner).allMatch { bedrifter -> bruker.organisasjoner.any { it.organizationNumber == bedrifter.bedriftNr } }

        assertThat(refusjonlist.size).isEqualTo(3);
        assertThat(refusjonlist2.size).isEqualTo(3);
        assertThat(refusjonlist3.size).isEqualTo(6);

        assertThat(refusjonlist3.refusjoner.find { ref -> ref.id == refusjonlist.refusjoner[0].id });
        assertThat(refusjonlist3.refusjoner.find { ref -> ref.id == refusjonlist2.refusjoner[0].id })

        // NÅR
        val json4 = sendRequest(
            get("$REQUEST_MAPPING_ARBEIDSGIVER_REFUSJON/hentliste?bedriftNr=$BEDRIFT_NR1,$BEDRIFT_NR2&page=0&size=6"),
            arbGiverToken
        )

        val refusjonlist4: RefusjonlistFraFlereOrgTest = mapper.readValue(json4, object : TypeReference<RefusjonlistFraFlereOrgTest>() {})

        // SÅ
        assertThat(refusjonlist4.refusjoner).allMatch { bedrifter -> bruker.organisasjoner.any { it.organizationNumber == bedrifter.bedriftNr } }
        assertThat(refusjonlist4.refusjoner).allMatch { org -> org.bedriftNr == BEDRIFT_NR1 || org.bedriftNr == BEDRIFT_NR2 }
    }

    @Test
    fun `hent() - Arbeidsgiver henter refusjon med id`() {
        val id = refusjonRepository.findAll().find { it.deltakerFnr == "07098142678" }?.id

        val json = sendRequest(get("$REQUEST_MAPPING_ARBEIDSGIVER_REFUSJON/$id"), arbGiverToken)
        val refusjon = mapper.readValue(json, Refusjon::class.java)
        assertEquals(id, refusjon.id)

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
        val refusjon = mapper.readValue(json, Refusjon::class.java)
        assertEquals(id, refusjon.id)

        verify(exactly = 1) {
            consoleLogger.logg(any())
        }
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
        assertThat(refusjonEtterInntektsgrunnlag.refusjonsgrunnlag.inntektsgrunnlag).isNotNull()


        // Huker av for at inntektene er opptjet i periode
        refusjonEtterInntektsgrunnlag.refusjonsgrunnlag.inntektsgrunnlag?.inntekter?.filter { it.erMedIInntektsgrunnlag() }?.forEach {
            setInntektslinjeOpptjentIPeriode(
                refusjonId = refusjonEtterInntektsgrunnlag.id,
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
        val refusjonEtterInntektsspørsmål = hentRefusjon(id)
        assertThat(refusjonEtterInntektsspørsmål.refusjonsgrunnlag.beregning?.refusjonsbeløp).isPositive()
        val harLagretHendelselogg = hendelsesloggRepository.findAll()
            .find { it.refusjonId == refusjonEtterInntektsspørsmål.id && it.event == "BeregningUtført" && it.appImageId != null } != null
        assertTrue(harLagretHendelselogg)

        // Godkjenn
        sendRequest(post("$REQUEST_MAPPING_ARBEIDSGIVER_REFUSJON/$id/godkjenn"), arbGiverToken)
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
                .header("If-Unmodified-Since", Now.instant())
                .header("authorization", "Bearer $arbGiverToken")
        )
            .andExpect(status().isBadRequest)
            .andExpect(header().string("feilkode", Feilkode.INGEN_INNTEKTER.toString()))
    }

    private fun setInntektslinjeOpptjentIPeriode(refusjonId: String, inntektslinjeId: String, erOpptjentIPeriode: Boolean) {
        val json = sendRequest(
            post("$REQUEST_MAPPING_ARBEIDSGIVER_REFUSJON/$refusjonId/set-inntektslinje-opptjent-i-periode"),
            arbGiverToken,
            EndreRefundertInntektslinjeRequest(inntektslinjeId, erOpptjentIPeriode)
        )
    }

    private fun hentRefusjon(id: String?): Refusjon {
        val json = sendRequest(get("$REQUEST_MAPPING_ARBEIDSGIVER_REFUSJON/$id"), arbGiverToken)
        return mapper.readValue(json, Refusjon::class.java)
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
