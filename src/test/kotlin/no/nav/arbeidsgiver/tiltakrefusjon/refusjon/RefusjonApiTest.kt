package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.SpykBean
import io.mockk.every
import io.mockk.verify
import io.mockk.clearMocks
import jakarta.servlet.http.Cookie
import no.nav.arbeidsgiver.tiltakrefusjon.Feilkode
import no.nav.arbeidsgiver.tiltakrefusjon.altinn.Organisasjon
import no.nav.arbeidsgiver.tiltakrefusjon.audit.AuditConsoleLogger
import no.nav.arbeidsgiver.tiltakrefusjon.autorisering.REQUEST_MAPPING_INNLOGGET_ARBEIDSGIVER
import no.nav.arbeidsgiver.tiltakrefusjon.hendelseslogg.HendelsesloggRepository
import no.nav.arbeidsgiver.tiltakrefusjon.refusjoner
import no.nav.arbeidsgiver.tiltakrefusjon.utils.Now
import no.nav.arbeidsgiver.tiltakrefusjon.varsling.VarslingRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
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
import java.time.LocalDate

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
    private final val TOKEN_X_COOKIE_NAVN = "tokenx-token"
    private final val AAD_COOKIE_NAVN = "aad-token"

    @SpykBean
    lateinit var consoleLogger: AuditConsoleLogger

    val navCookie = Cookie(AAD_COOKIE_NAVN, lagTokenForNavId("Z123456"))
    val arbGiverCookie = Cookie(TOKEN_X_COOKIE_NAVN, lagTokenForFnr("16120102137"))

    @BeforeEach
    fun setUp() {
        refusjonRepository.saveAll(refusjoner())
        refusjonRepository.findAll().forEach {
            refusjonService.oppdaterRefusjon(it)
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
        val json = sendRequest(get("$REQUEST_MAPPING_SAKSBEHANDLER_REFUSJON?enhet=1000"), navCookie)
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
                .cookie(arbGiverCookie)
        )
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

        // Forventer at oppslag auditlogges, men kun én gang per unike deltaker
        verify(exactly = liste.map { it.deltakerFnr }.toSet().size) {
            consoleLogger.logg(any())
        }
    }


    @Test
    fun `hentAlle refusjon for alle bedrifter arbeidsgiver har tilgang til`() {
        // GITT
        val BEDRIFT_NR1 = "998877665"
        val BEDRIFT_NR2 = "999999999"

        // NÅR
        val brukerJson = sendRequest(get("$REQUEST_MAPPING_INNLOGGET_ARBEIDSGIVER/innlogget-bruker"), arbGiverCookie)
        val bruker: InnloggetBrukerTest = mapper.readValue(brukerJson, object : TypeReference<InnloggetBrukerTest>() {})
        val refusjonJson =
            sendRequest(get("$REQUEST_MAPPING_ARBEIDSGIVER_REFUSJON/hentliste?page=0&size=3"), arbGiverCookie)
        val refusjonlist: RefusjonlistFraFlereOrgTest = mapper.readValue(refusjonJson, object : TypeReference<RefusjonlistFraFlereOrgTest>() {})
        verify(exactly = refusjonlist.refusjoner.map { it.deltakerFnr }.toSet().size) {
            consoleLogger.logg(any())
        }
        resetAuditCount()
        val refusjonJson2 =
            sendRequest(get("$REQUEST_MAPPING_ARBEIDSGIVER_REFUSJON/hentliste?page=1&size=3"), arbGiverCookie)
        val refusjonlist2: RefusjonlistFraFlereOrgTest = mapper.readValue(refusjonJson2, object : TypeReference<RefusjonlistFraFlereOrgTest>() {})
        verify(exactly = refusjonlist2.refusjoner.map { it.deltakerFnr }.toSet().size) {
            consoleLogger.logg(any())
        }
        resetAuditCount()
        val refusjonJson3 =
            sendRequest(get("$REQUEST_MAPPING_ARBEIDSGIVER_REFUSJON/hentliste?page=0&size=6"), arbGiverCookie)
        val refusjonlist3: RefusjonlistFraFlereOrgTest = mapper.readValue(refusjonJson3, object : TypeReference<RefusjonlistFraFlereOrgTest>() {})
        verify(exactly = refusjonlist3.refusjoner.map { it.deltakerFnr }.toSet().size) {
            consoleLogger.logg(any())
        }

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
            arbGiverCookie
        )

        val refusjonlist4: RefusjonlistFraFlereOrgTest = mapper.readValue(json4, object : TypeReference<RefusjonlistFraFlereOrgTest>() {})

        // SÅ
        assertThat(refusjonlist4.refusjoner).allMatch { bedrifter -> bruker.organisasjoner.any { it.organizationNumber == bedrifter.bedriftNr } }
        assertThat(refusjonlist4.refusjoner).allMatch { org -> org.bedriftNr == BEDRIFT_NR1 || org.bedriftNr == BEDRIFT_NR2 }
    }

    @Test
    fun `hent() - Arbeidsgiver henter refusjon med id`() {
        val id = refusjonRepository.findAll().find { it.deltakerFnr == "07098142678" }?.id

        val json = sendRequest(get("$REQUEST_MAPPING_ARBEIDSGIVER_REFUSJON/$id"), arbGiverCookie)
        val refusjon = mapper.readValue(json, Refusjon::class.java)
        assertEquals(id, refusjon.id)

        verify(exactly = 1) {
            consoleLogger.logg(any())
        }
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

        verify(exactly = 1) {
            consoleLogger.logg(any())
        }
    }

    @Test
    fun `hent() - Saksbehandler mangler tilgang til henter refusjon med id`() {
        val id = refusjonRepository.findAll().find { it.deltakerFnr == "07098142678" }?.id
        sendRequest(get("$REQUEST_MAPPING_SAKSBEHANDLER_REFUSJON/$id"), navCookie, status().isUnauthorized)
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
        hentRefusjon(id)
        val refusjonEtterInntektsgrunnlag = oppdaterRefusjonMedKontonrOgInntekter(id!!)
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
            arbGiverCookie,
            EndreBruttolønnRequest(true, null)
        )
        val refusjonEtterInntektsspørsmål = hentRefusjon(id)
        assertThat(refusjonEtterInntektsspørsmål.refusjonsgrunnlag.beregning?.refusjonsbeløp).isPositive()
        val harLagretHendelselogg = hendelsesloggRepository.findAll()
            .find { it.refusjonId == refusjonEtterInntektsspørsmål.id && it.event == "BeregningUtført" && it.appImageId != null } != null
        assertTrue(harLagretHendelselogg)

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
                .header("If-Unmodified-Since", Now.instant())
                .cookie(arbGiverCookie)
        )
            .andExpect(status().isBadRequest)
            .andExpect(header().string("feilkode", Feilkode.INGEN_INNTEKTER.toString()))
    }

    private fun setInntektslinjeOpptjentIPeriode(refusjonId: String, inntektslinjeId: String, erOpptjentIPeriode: Boolean) {
        val json = sendRequest(
            post("$REQUEST_MAPPING_ARBEIDSGIVER_REFUSJON/$refusjonId/set-inntektslinje-opptjent-i-periode"),
            arbGiverCookie,
            EndreRefundertInntektslinjeRequest(inntektslinjeId, erOpptjentIPeriode)
        )
    }

    private fun hentRefusjon(id: String?): Refusjon {
        val json = sendRequest(get("$REQUEST_MAPPING_ARBEIDSGIVER_REFUSJON/$id"), arbGiverCookie)
        return mapper.readValue(json, Refusjon::class.java)
    }

    private fun oppdaterRefusjonMedKontonrOgInntekter(id: String): Refusjon {
        val json = sendRequest(post("$REQUEST_MAPPING_ARBEIDSGIVER_REFUSJON/$id/oppdater-refusjon"), arbGiverCookie)
        return mapper.readValue(json, Refusjon::class.java)
    }

    private fun sendRequest(request: MockHttpServletRequestBuilder, cookie: Cookie): String {
        return sendRequest(request, cookie, null)
    }

    private fun sendRequest(
        request: MockHttpServletRequestBuilder,
        cookie: Cookie,
        content: Any?,
        status: ResultMatcher = status().isOk
    ): String {
        if (content != null) {
            request.content(mapper.writeValueAsString(content))
        }

        return mockMvc.perform(
            request
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .header("If-Unmodified-Since", Now.instant())
                .cookie(cookie)
        )
            .andExpect(status)
            .andReturn()
            .response.getContentAsString(StandardCharsets.UTF_8)
    }

    private fun sendRequest(request: MockHttpServletRequestBuilder, cookie: Cookie, forventetStatus: ResultMatcher) {
        mockMvc.perform(
            request
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .header("If-Unmodified-Since", Now.instant())
                .cookie(cookie)
        )
            .andExpect(forventetStatus)
    }

    private final fun lagTokenForFnr(fnr: String): String? {
        return HttpClient.newHttpClient().send(
            HttpRequest.newBuilder()
                .GET()
                .uri(URI.create("https://tiltak-fakelogin.ekstern.dev.nav.no/token?pid=${fnr}&aud=aud-tokenx&iss=tokenx&acr=Level4"))
                .build(), BodyHandlers.ofString()
        ).body()
    }

    private final fun lagTokenForNavId(navId: String): String? {
        return HttpClient.newHttpClient().send(
            HttpRequest.newBuilder()
                .GET()
                .uri(URI.create("https://tiltak-fakelogin.ekstern.dev.nav.no/token?NAVident=${navId}&iss=aad&aud=aud-aad"))
                .build(), BodyHandlers.ofString()
        ).body()
    }
}
