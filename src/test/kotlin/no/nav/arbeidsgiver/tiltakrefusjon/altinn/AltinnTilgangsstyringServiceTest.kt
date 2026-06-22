package no.nav.arbeidsgiver.tiltakrefusjon.altinn

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.mockk.every
import io.mockk.spyk
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.Fnr
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.FnrTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock
import org.springframework.test.context.ActiveProfiles
import org.springframework.web.client.RestTemplate
import java.net.URI


@SpringBootTest
@ActiveProfiles("local")
@AutoConfigureWireMock
class AltinnTilgangsstyringServiceTest {

    @Autowired
    lateinit var altinnTilgangsstyringProperties: AltinnTilgangsstyringProperties
    @Autowired
    lateinit var altinnTilgangsstyringService: AltinnTilgangsstyringService
    @Autowired
    lateinit var altinnTilgangsstyringKlient: AltinnTilgangsstyringKlient
    @Value("\${tiltak-refusjon.altinn-tilgangstyring.arbeidsgiverAltinnTilgangerUri}")
    lateinit var opprinneligAltinnMockUrl: String
    @Value("\${wiremock.server.port}")
    lateinit var wiremockPort: String

    @AfterEach
    fun cleanUp() {
        altinnTilgangsstyringProperties.arbeidsgiverAltinnTilgangerUri = URI.create(opprinneligAltinnMockUrl)
    }

    @Test
    fun `skal returnere en liste med 18 organisasjoner personen har tilgang til`() {
        // NÅR
        val organisasjoner: Set<Organisasjon> = altinnTilgangsstyringService.hentInntektsmeldingEllerRefusjonTilganger(Fnr.generer(2024, 1, 1).verdi)

        // SÅ
        assertThat(organisasjoner).hasSize(18)
    }

    @Test
    fun `skal returnere en tom liste med organisasjoner om personen ikke har tilgang`() {
        // GITT
        altinnTilgangsstyringProperties.arbeidsgiverAltinnTilgangerUri = URI.create("http://localhost:${wiremockPort}/altinn-tilganger-tom")

        // NÅR
        val organisasjoner: Set<Organisasjon> = altinnTilgangsstyringService.hentInntektsmeldingEllerRefusjonTilganger(Fnr.generer(2024, 1, 1).verdi)

        // SÅ
        assertThat(organisasjoner).hasSize(0)
    }

    @Test
    fun `hentInntektsmeldingEllerRefusjonTilganger skal flate ting ut`() {
        val responseFraAltinn3 =
            "[{\"orgnr\":\"811306312\",\"altinn3Tilganger\":[\"nav_tiltak_tiltaksrefusjon\",\"nav_rekruttering_stillingsannonser\",\"nav_foreldrepenger_inntektsmelding\",\"nav_forebygge-og-redusere-sykefravar_samarbeid\"],\"altinn2Tilganger\":[\"5934:1\",\"4826:1\",\"5902:1\",\"4936:1\",\"5078:1\"],\"underenheter\":[{\"orgnr\":\"811306622\",\"altinn3Tilganger\":[\"nav_tiltak_tiltaksrefusjon\",\"nav_rekruttering_stillingsannonser\",\"nav_foreldrepenger_inntektsmelding\",\"nav_forebygge-og-redusere-sykefravar_samarbeid\"],\"altinn2Tilganger\":[\"5934:1\",\"4826:1\",\"5902:1\",\"4936:1\",\"5078:1\"],\"underenheter\":[],\"navn\":\"DAVIK OG EIDSLANDET\",\"organisasjonsform\":\"BEDR\",\"erSlettet\":false},{\"orgnr\":\"811307432\",\"altinn3Tilganger\":[\"nav_tiltak_tiltaksrefusjon\",\"nav_rekruttering_stillingsannonser\",\"nav_foreldrepenger_inntektsmelding\",\"nav_forebygge-og-redusere-sykefravar_samarbeid\"],\"altinn2Tilganger\":[\"5934:1\",\"4826:1\",\"5902:1\",\"4936:1\",\"5078:1\"],\"underenheter\":[],\"navn\":\"DAVIK OG ULNES\",\"organisasjonsform\":\"BEDR\",\"erSlettet\":false},{\"orgnr\":\"811307122\",\"altinn3Tilganger\":[\"nav_tiltak_tiltaksrefusjon\",\"nav_rekruttering_stillingsannonser\",\"nav_foreldrepenger_inntektsmelding\",\"nav_forebygge-og-redusere-sykefravar_samarbeid\"],\"altinn2Tilganger\":[\"5934:1\",\"4826:1\",\"5902:1\",\"4936:1\",\"5078:1\"],\"underenheter\":[],\"navn\":\"DAVIK OG SÆTERVIK\",\"organisasjonsform\":\"BEDR\",\"erSlettet\":false},{\"orgnr\":\"811306932\",\"altinn3Tilganger\":[\"nav_tiltak_tiltaksrefusjon\",\"nav_rekruttering_stillingsannonser\",\"nav_foreldrepenger_inntektsmelding\",\"nav_forebygge-og-redusere-sykefravar_samarbeid\"],\"altinn2Tilganger\":[\"5934:1\",\"4826:1\",\"5902:1\",\"4936:1\",\"5078:1\"],\"underenheter\":[],\"navn\":\"DAVIK OG HAMARØY\",\"organisasjonsform\":\"BEDR\",\"erSlettet\":false},{\"orgnr\":\"811307602\",\"altinn3Tilganger\":[\"nav_tiltak_tiltaksrefusjon\",\"nav_rekruttering_stillingsannonser\",\"nav_foreldrepenger_inntektsmelding\",\"nav_forebygge-og-redusere-sykefravar_samarbeid\"],\"altinn2Tilganger\":[\"5934:1\",\"4826:1\",\"5902:1\",\"4936:1\",\"5078:1\"],\"underenheter\":[],\"navn\":\"DAVIK OG ABELVÆR\",\"organisasjonsform\":\"BEDR\",\"erSlettet\":false}],\"navn\":\"DAVIK OG HORTEN\",\"organisasjonsform\":\"AS\",\"erSlettet\":false},{\"orgnr\":\"810825472\",\"altinn3Tilganger\":[\"nav_arbeidsforhold_aa-registeret-innsyn-arbeidsgiver\",\"nav_tiltak_tiltaksrefusjon\",\"nav_rekruttering_stillingsannonser\",\"nav_utbetaling_endre-kontonummer-refusjon-arbeidsgiver\",\"nav_arbeidsforhold_aa-registeret-sok-tilgang\",\"nav_forebygge-og-redusere-sykefravar_sykefravarsstatistikk\",\"nav-migratedcorrespondence-5516-7\",\"nav_permittering-og-nedbemmaning_innsyn-i-alle-innsendte-meldinger\",\"nav_foreldrepenger_inntektsmelding\",\"nav_arbeidsforhold_aa-registeret-brukerstotte\",\"nav_forebygge-og-redusere-sykefravar_samarbeid\",\"nav_sosialtjenester_digisos-avtale\"],\"altinn2Tilganger\":[\"3403:1\",\"5332:1\",\"5867:1\",\"5810:1\",\"4936:1\",\"5384:1\",\"5516:3\",\"5516:4\",\"5719:1\",\"5516:1\",\"5516:2\",\"5934:1\",\"5516:7\",\"5441:2\",\"5516:5\",\"5516:6\",\"2896:87\",\"5441:1\",\"4826:1\",\"5902:1\",\"5078:1\",\"5278:1\"],\"underenheter\":[{\"orgnr\":\"910825526\",\"altinn3Tilganger\":[\"nav_rekruttering_stillingsannonser\",\"test-fager\",\"nav_foreldrepenger_inntektsmelding\",\"nav_forebygge-og-redusere-sykefravar_samarbeid\",\"nav_arbeidsforhold_aa-registeret-innsyn-arbeidsgiver\",\"nav_tiltak_tiltaksrefusjon\",\"nav_utbetaling_endre-kontonummer-refusjon-arbeidsgiver\",\"nav_arbeidsforhold_aa-registeret-sok-tilgang\",\"nav_forebygge-og-redusere-sykefravar_sykefravarsstatistikk\",\"nav-migratedcorrespondence-5516-7\",\"nav_permittering-og-nedbemmaning_innsyn-i-alle-innsendte-meldinger\",\"nav_arbeidsforhold_aa-registeret-brukerstotte\",\"nav-migratedcorrespondence-5516-2\",\"nav-migratedcorrespondence-5516-1\",\"nav_sosialtjenester_digisos-avtale\"],\"altinn2Tilganger\":[\"3403:1\",\"5332:1\",\"5867:1\",\"5810:1\",\"4936:1\",\"5384:1\",\"5516:3\",\"5516:4\",\"5719:1\",\"5516:1\",\"5516:2\",\"5934:1\",\"5516:7\",\"5441:2\",\"5516:5\",\"5516:6\",\"2896:87\",\"5441:1\",\"4826:1\",\"5902:1\",\"5078:1\",\"5278:1\"],\"underenheter\":[],\"navn\":\"GAMLE FREDRIKSTAD OG RAMNES REGNSK\",\"organisasjonsform\":\"BEDR\",\"erSlettet\":false},{\"orgnr\":\"910825496\",\"altinn3Tilganger\":[\"nav_rekruttering_stillingsannonser\",\"test-fager\",\"nav_foreldrepenger_inntektsmelding\",\"nav_forebygge-og-redusere-sykefravar_samarbeid\",\"nav_arbeidsforhold_aa-registeret-innsyn-arbeidsgiver\",\"nav_tiltak_tiltaksrefusjon\",\"nav_utbetaling_endre-kontonummer-refusjon-arbeidsgiver\",\"nav_arbeidsforhold_aa-registeret-sok-tilgang\",\"nav_forebygge-og-redusere-sykefravar_sykefravarsstatistikk\",\"nav-migratedcorrespondence-5516-7\",\"nav_permittering-og-nedbemmaning_innsyn-i-alle-innsendte-meldinger\",\"nav_arbeidsforhold_aa-registeret-brukerstotte\",\"nav_sosialtjenester_digisos-avtale\"],\"altinn2Tilganger\":[\"3403:1\",\"5332:1\",\"5867:1\",\"5810:1\",\"4936:1\",\"5384:1\",\"5516:3\",\"5516:4\",\"5719:1\",\"5516:1\",\"5516:2\",\"5934:1\",\"5516:7\",\"5441:2\",\"5516:5\",\"5516:6\",\"2896:87\",\"5441:1\",\"4826:1\",\"5902:1\",\"5078:1\",\"5278:1\"],\"underenheter\":[],\"navn\":\"SLEMMESTAD OG STAVERN REGNSKAP\",\"organisasjonsform\":\"BEDR\",\"erSlettet\":false},{\"orgnr\":\"910825518\",\"altinn3Tilganger\":[\"nav_arbeidsforhold_aa-registeret-innsyn-arbeidsgiver\",\"nav_tiltak_tiltaksrefusjon\",\"nav_rekruttering_stillingsannonser\",\"nav_utbetaling_endre-kontonummer-refusjon-arbeidsgiver\",\"nav_arbeidsforhold_aa-registeret-sok-tilgang\",\"nav_forebygge-og-redusere-sykefravar_sykefravarsstatistikk\",\"nav-migratedcorrespondence-5516-7\",\"nav_permittering-og-nedbemmaning_innsyn-i-alle-innsendte-meldinger\",\"nav_foreldrepenger_inntektsmelding\",\"nav_arbeidsforhold_aa-registeret-brukerstotte\",\"nav_forebygge-og-redusere-sykefravar_samarbeid\",\"nav_sosialtjenester_digisos-avtale\"],\"altinn2Tilganger\":[\"3403:1\",\"5332:1\",\"5867:1\",\"5810:1\",\"4936:1\",\"5384:1\",\"5516:3\",\"5516:4\",\"5719:1\",\"5516:1\",\"5516:2\",\"5934:1\",\"5516:7\",\"5441:2\",\"5516:5\",\"5516:6\",\"2896:87\",\"5441:1\",\"4826:1\",\"5902:1\",\"5078:1\",\"5278:1\"],\"underenheter\":[],\"navn\":\"MAURA OG KOLBU REGNSKAP\",\"organisasjonsform\":\"BEDR\",\"erSlettet\":false}],\"navn\":\"MALMEFJORDEN OG RIDABU REGNSKAP\",\"organisasjonsform\":\"AS\",\"erSlettet\":false},{\"orgnr\":\"910712217\",\"altinn3Tilganger\":[\"nav_tiltak_tiltaksrefusjon\",\"nav_rekruttering_stillingsannonser\",\"nav_forebygge-og-redusere-sykefravar_sykefravarsstatistikk\",\"nav_foreldrepenger_inntektsmelding\",\"nav_forebygge-og-redusere-sykefravar_samarbeid\"],\"altinn2Tilganger\":[\"5934:1\",\"3403:1\",\"5332:1\",\"2896:87\",\"4826:1\",\"5902:1\",\"4936:1\",\"5384:1\",\"5078:1\"],\"underenheter\":[{\"orgnr\":\"910712241\",\"altinn3Tilganger\":[\"nav_tiltak_tiltaksrefusjon\",\"nav_rekruttering_stillingsannonser\",\"nav_forebygge-og-redusere-sykefravar_sykefravarsstatistikk\",\"nav_foreldrepenger_inntektsmelding\",\"nav_forebygge-og-redusere-sykefravar_samarbeid\"],\"altinn2Tilganger\":[\"5934:1\",\"3403:1\",\"5332:1\",\"2896:87\",\"4826:1\",\"5902:1\",\"4936:1\",\"5384:1\",\"5078:1\"],\"underenheter\":[],\"navn\":\"ULNES OG SÆBØ\",\"organisasjonsform\":\"BEDR\",\"erSlettet\":false},{\"orgnr\":\"910712268\",\"altinn3Tilganger\":[\"nav_tiltak_tiltaksrefusjon\",\"nav_rekruttering_stillingsannonser\",\"nav_forebygge-og-redusere-sykefravar_sykefravarsstatistikk\",\"nav_foreldrepenger_inntektsmelding\",\"nav_forebygge-og-redusere-sykefravar_samarbeid\"],\"altinn2Tilganger\":[\"5934:1\",\"3403:1\",\"5332:1\",\"2896:87\",\"4826:1\",\"5902:1\",\"4936:1\",\"5384:1\",\"5078:1\"],\"underenheter\":[],\"navn\":\"ENEBAKK OG ØYER\",\"organisasjonsform\":\"BEDR\",\"erSlettet\":false},{\"orgnr\":\"910712233\",\"altinn3Tilganger\":[\"nav_tiltak_tiltaksrefusjon\",\"nav_rekruttering_stillingsannonser\",\"nav_forebygge-og-redusere-sykefravar_sykefravarsstatistikk\",\"nav_foreldrepenger_inntektsmelding\",\"nav_forebygge-og-redusere-sykefravar_samarbeid\"],\"altinn2Tilganger\":[\"5934:1\",\"3403:1\",\"5332:1\",\"2896:87\",\"4826:1\",\"5902:1\",\"4936:1\",\"5384:1\",\"5078:1\"],\"underenheter\":[],\"navn\":\"UTVIK OG ETNE\",\"organisasjonsform\":\"BEDR\",\"erSlettet\":false}],\"navn\":\"STØ OG BERGER\",\"organisasjonsform\":\"AS\",\"erSlettet\":false},{\"orgnr\":\"910825550\",\"altinn3Tilganger\":[\"nav_tiltak_tiltaksrefusjon\",\"nav_rekruttering_stillingsannonser\",\"nav_forebygge-og-redusere-sykefravar_sykefravarsstatistikk\",\"nav_foreldrepenger_inntektsmelding\",\"nav_forebygge-og-redusere-sykefravar_samarbeid\"],\"altinn2Tilganger\":[\"5934:1\",\"3403:1\",\"5332:1\",\"2896:87\",\"4826:1\",\"5902:1\",\"4936:1\",\"5384:1\",\"5078:1\"],\"underenheter\":[{\"orgnr\":\"910825585\",\"altinn3Tilganger\":[\"nav_tiltak_tiltaksrefusjon\",\"nav_rekruttering_stillingsannonser\",\"nav_forebygge-og-redusere-sykefravar_sykefravarsstatistikk\",\"nav_foreldrepenger_inntektsmelding\",\"nav_forebygge-og-redusere-sykefravar_samarbeid\",\"nav-migratedcorrespondence-5278-1\",\"nav-migratedcorrespondence-5515-1\"],\"altinn2Tilganger\":[\"5934:1\",\"3403:1\",\"5332:1\",\"2896:87\",\"4826:1\",\"5902:1\",\"4936:1\",\"5384:1\",\"5078:1\",\"5278:1\"],\"underenheter\":[],\"navn\":\"LINESØYA OG LANGANGEN REGNSKAP\",\"organisasjonsform\":\"BEDR\",\"erSlettet\":false},{\"orgnr\":\"910825607\",\"altinn3Tilganger\":[\"nav_tiltak_tiltaksrefusjon\",\"nav_rekruttering_stillingsannonser\",\"nav_forebygge-og-redusere-sykefravar_sykefravarsstatistikk\",\"nav_foreldrepenger_inntektsmelding\",\"nav_forebygge-og-redusere-sykefravar_samarbeid\",\"nav-migratedcorrespondence-5278-1\"],\"altinn2Tilganger\":[\"5934:1\",\"3403:1\",\"5332:1\",\"2896:87\",\"4826:1\",\"5902:1\",\"4936:1\",\"5384:1\",\"5078:1\",\"5278:1\"],\"underenheter\":[],\"navn\":\"BIRTAVARRE OG VÆRLANDET REGNSKAP\",\"organisasjonsform\":\"BEDR\",\"erSlettet\":false},{\"orgnr\":\"910825569\",\"altinn3Tilganger\":[\"nav_tiltak_tiltaksrefusjon\",\"nav_rekruttering_stillingsannonser\",\"nav_forebygge-og-redusere-sykefravar_sykefravarsstatistikk\",\"nav_foreldrepenger_inntektsmelding\",\"nav_forebygge-og-redusere-sykefravar_samarbeid\"],\"altinn2Tilganger\":[\"5934:1\",\"3403:1\",\"5332:1\",\"2896:87\",\"4826:1\",\"5902:1\",\"4936:1\",\"5384:1\",\"5078:1\"],\"underenheter\":[],\"navn\":\"STORFOSNA OG FREDRIKSTAD REGNSKAP\",\"organisasjonsform\":\"BEDR\",\"erSlettet\":false}],\"navn\":\"TRANØY OG SANDE I VESTFOLD REGNSKA\",\"organisasjonsform\":\"AS\",\"erSlettet\":false}]";
        val objectMapper = jacksonObjectMapper()
        val responseFraAltinn3List: List<AltinnTilgang> = objectMapper.readValue(responseFraAltinn3)

        // Create a spy on the real service instead of using a complete mock
        val klientSpy = spyk(altinnTilgangsstyringKlient)
        val serviceSpy = spyk(altinnTilgangsstyringService)
        // Only mock the kallAltinn3 method
        every { klientSpy.kallAltinn3(any()) } returns responseFraAltinn3List
        // Use the spy to call the real method which will use our mocked kallAltinn3 internally
        val organisasjoner2 = serviceSpy.hentInntektsmeldingEllerRefusjonTilganger(Fnr.generer(2024, 1, 1).verdi)

        assertThat(organisasjoner2).isNotEmpty
        //TODO: Teste noe litt mer spennende her..

    }

}
