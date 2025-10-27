package no.nav.arbeidsgiver.tiltakrefusjon.featuretoggles

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import no.nav.arbeidsgiver.tiltakrefusjon.altinn.AltinnTilgang
import no.nav.arbeidsgiver.tiltakrefusjon.altinn.AltinnTilgangsstyringService
import no.nav.arbeidsgiver.tiltakrefusjon.altinn.Organisasjon
import no.nav.arbeidsgiver.tiltakrefusjon.autorisering.AdGruppeTilganger
import no.nav.arbeidsgiver.tiltakrefusjon.autorisering.InnloggetArbeidsgiver
import no.nav.arbeidsgiver.tiltakrefusjon.autorisering.InnloggetSaksbehandler
import no.nav.arbeidsgiver.tiltakrefusjon.autorisering.TilgangskontrollService
import no.nav.arbeidsgiver.tiltakrefusjon.innloggetBruker
import no.nav.arbeidsgiver.tiltakrefusjon.inntekt.FakeInntektskomponentService
import no.nav.arbeidsgiver.tiltakrefusjon.norg.NorgService
import no.nav.arbeidsgiver.tiltakrefusjon.okonomi.FakeKontoregisterService
import no.nav.arbeidsgiver.tiltakrefusjon.persondata.PersondataService
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.BrukerRolle
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.HentSaksbehandlerRefusjonerQueryParametre
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.KorreksjonRepository
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.RefusjonRepository
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.RefusjonService
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.Tiltakstype
import no.nav.arbeidsgiver.tiltakrefusjon.refusjoner
import no.nav.arbeidsgiver.tiltakrefusjon.tilskuddsperiode.TilskuddsperiodeGodkjentMelding
import no.nav.arbeidsgiver.tiltakrefusjon.utils.Now
import no.nav.team_tiltak.felles.persondata.pdl.domene.Diskresjonskode
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDateTime
import java.util.*

@SpringBootTest
@ActiveProfiles("local")
@AutoConfigureMockMvc
@AutoConfigureWireMock(port = 8091)
class MentorFeatureToggleTest(
    @Autowired
    val refusjonService: RefusjonService,
    @Autowired
    val refusjonRepository: RefusjonRepository,
    @Autowired
    val tilgangskontrollService: TilgangskontrollService,
    @Autowired
    val inntektskomponentService: FakeInntektskomponentService,
    @Autowired
    val kontoregisterService: FakeKontoregisterService,
    @Autowired
    val norgService: NorgService,

    ) {


    @MockkBean
    lateinit var altinnTilgangsstyringService: AltinnTilgangsstyringService

    @MockkBean
    lateinit var persondataService: PersondataService

    @MockkBean
    lateinit var korreksjonRepository: KorreksjonRepository

    @MockkBean
    lateinit var featureToggleServiceMock: FeatureToggleService

    val arbeidsgiverIdent = innloggetBruker(
        "16120102137",
        BrukerRolle.ARBEIDSGIVER
    )
    val innloggetVeileder = innloggetBruker("X123456", BrukerRolle.VEILEDER);
    val saksbehandlerIdent = innloggetBruker(
        "Z123456",
        BrukerRolle.BESLUTTER
    )

    @BeforeEach
    fun setUp() {
        refusjonRepository.deleteAll()
        refusjonRepository.saveAll(refusjoner())
        val organisasjon: Organisasjon = Organisasjon(
            "Bedrift AS",
            "Bedrift type",
            "999999999",
            "Org form",
            "Status"
        )
        val altinnTilgang: AltinnTilgang = AltinnTilgang(
            organisasjon.organizationNumber,
            setOf(),
            setOf(),
            listOf(),
            organisasjon.name,
            organisasjon.organizationForm,
        )
        every { altinnTilgangsstyringService.altinnTilgangsstyringProperties.inntektsmeldingServiceCode } returns 4936
        every { altinnTilgangsstyringService.altinnTilgangsstyringProperties.inntektsmeldingServiceEdition } returns 1
        every { persondataService.hentDiskresjonskode(any()) } returns Diskresjonskode.UGRADERT
        every { altinnTilgangsstyringService.hentAdressesperreTilganger(any()) } returns setOf<Organisasjon>(
            organisasjon
        )
        every {
            altinnTilgangsstyringService.hentInntektsmeldingTilganger(
                any()
            )
        } returns setOf<Organisasjon>(
            organisasjon
        )
        every { altinnTilgangsstyringService.hentInntektsmeldingEllerRefusjonTilganger() } returns setOf<Organisasjon>(
            organisasjon
        )
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun mentorToggle_off(mentorFeatureToggle: Boolean) {
        every {
            featureToggleServiceMock.isEnabled(
                FeatureToggle.MENTOR_TILSKUDD,
                arbeidsgiverIdent.identifikator
            )
        }.returns(mentorFeatureToggle)

        val mentorFeature: Boolean = featureToggleServiceMock.isEnabled(FeatureToggle.MENTOR_TILSKUDD, "16120102137")


        assertThat(mentorFeature).isEqualTo(mentorFeatureToggle)
    }


    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun `Finn refusjon for innlogget arbeidsgiver ska returnere mentoravtaler om toggle er paa`(mentorFeatureToggle: Boolean) {
        every {
            featureToggleServiceMock.isEnabled(
                FeatureToggle.MENTOR_TILSKUDD,
                arbeidsgiverIdent.identifikator
            )
        } returns mentorFeatureToggle

        val bedriftNr = "999999999"
        val mentorFnr = "07077712345"
        val vanligFnr = "06066612345"

        val mentorTilskudd = TilskuddsperiodeGodkjentMelding(
            avtaleId = "mentor-avtale",
            tilskuddsbeløp = 1000,
            tiltakstype = Tiltakstype.MENTOR,
            deltakerEtternavn = "Mentor",
            deltakerFornavn = "Mina",
            arbeidsgiverFornavn = "Arne",
            arbeidsgiverEtternavn = "Arbeidsgiver",
            arbeidsgiverTlf = "41111111",
            arbeidsgiveravgiftSats = 0.141,
            avtaleInnholdId = "m1",
            bedriftNavn = "Bedriften AS",
            bedriftNr = bedriftNr,
            deltakerFnr = mentorFnr,
            feriepengerSats = 0.125,
            otpSats = 0.03,
            tilskuddFom = Now.localDate().minusWeeks(4),
            tilskuddTom = Now.localDate().minusDays(1),
            tilskuddsperiodeId = "mentor-periode",
            veilederNavIdent = "X123456",
            lønnstilskuddsprosent = 0,
            avtaleNr = 9991,
            løpenummer = 1,
            resendingsnummer = null,
            enhet = "1000",
            godkjentTidspunkt = java.time.LocalDateTime.now(),
            arbeidsgiverKontonummer = "12345678908",
            arbeidsgiverKid = null,
            mentorTimelonn = 500,
            mentorAntallTimer = 15.0
        )
        val vanligTilskudd = mentorTilskudd.copy(
            avtaleId = "vanlig-avtale",
            tiltakstype = Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD,
            deltakerEtternavn = "Vanlig",
            deltakerFornavn = "Vera",
            avtaleInnholdId = "v1",
            deltakerFnr = vanligFnr,
            tilskuddsperiodeId = "vanlig-periode",
            avtaleNr = 8882,
            lønnstilskuddsprosent = 60
        )

        val mentorRefusjon = refusjonService.opprettRefusjon(mentorTilskudd)!!
        val vanligRefusjon = refusjonService.opprettRefusjon(vanligTilskudd)!!

        assertThat(refusjonRepository.findById(mentorRefusjon.id)).isPresent
        assertThat(refusjonRepository.findById(vanligRefusjon.id)).isPresent

        val innloggetArbeidsgiver = InnloggetArbeidsgiver(
            identifikator = "16120102137",
            altinnTilgangsstyringService = altinnTilgangsstyringService,
            refusjonRepository = refusjonRepository,
            korreksjonRepository = korreksjonRepository,
            refusjonService = refusjonService,
            persondataService = persondataService,
            featureToggleService = featureToggleServiceMock
        )

        val page = innloggetArbeidsgiver.finnAlleForGittArbeidsgiver(
            bedrifter = "ALLEBEDRIFTER",
            status = null,
            tiltakstype = null,
            sortingOrder = null,
            page = 0,
            size = 50
        )

        assertThat(page.content.any { it.id == mentorRefusjon.id }).isEqualTo(mentorFeatureToggle)
        assertThat(page.content.any { it.id == vanligRefusjon.id }).isTrue()
        assertThat(page.content.none { it.tiltakstype() == Tiltakstype.MENTOR }).isNotEqualTo(mentorFeatureToggle)
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun `finnAlle for saksbehandler skal inkludere mentor bare når toggle er på`(mentorToggle: Boolean) {
        every {
            featureToggleServiceMock.isEnabled(
                FeatureToggle.MENTOR_TILSKUDD,
                saksbehandlerIdent.identifikator
            )
        } returns mentorToggle

        val saksbehandler: InnloggetSaksbehandler = InnloggetSaksbehandler(
            identifikator = saksbehandlerIdent.identifikator,
            azureOid = UUID.fromString("550e8400-e29b-41d4-a716-446655440000"),
            navn = "Saks Behandler",
            tilgangskontrollService = tilgangskontrollService,
            norgeService = norgService,
            refusjonRepository = refusjonRepository,
            korreksjonRepository = korreksjonRepository,
            refusjonService = refusjonService,
            inntektskomponentService = inntektskomponentService,
            kontoregisterService = kontoregisterService,
            adGruppeTilganger = AdGruppeTilganger(
                beslutter = true,
                korreksjon = true,
                fortroligAdresse = false,
                strengtFortroligAdresse = false
            ),
            persondataService = persondataService,
            featureToggleService = featureToggleServiceMock
        )

        val bedriftNr = "999999999"
        val mentorFnr = "07077712345"
        val vanligFnr = "06066612345"

        val mentorTilskudd = TilskuddsperiodeGodkjentMelding(
            avtaleId = "mentor-avtale",
            tilskuddsbeløp = 1000,
            tiltakstype = Tiltakstype.MENTOR,
            deltakerEtternavn = "Mentor",
            deltakerFornavn = "Mina",
            arbeidsgiverFornavn = "Arne",
            arbeidsgiverEtternavn = "Arbeidsgiver",
            arbeidsgiverTlf = "41111111",
            arbeidsgiveravgiftSats = 0.141,
            avtaleInnholdId = "m1",
            bedriftNavn = "Bedriften AS",
            bedriftNr = bedriftNr,
            deltakerFnr = mentorFnr,
            feriepengerSats = 0.125,
            otpSats = 0.03,
            tilskuddFom = Now.localDate().minusWeeks(4),
            tilskuddTom = Now.localDate().minusDays(1),
            tilskuddsperiodeId = "mentor-periode",
            veilederNavIdent = "X123456",
            lønnstilskuddsprosent = 0,
            avtaleNr = 9991,
            løpenummer = 1,
            resendingsnummer = null,
            enhet = "1000",
            godkjentTidspunkt = LocalDateTime.now(),
            arbeidsgiverKontonummer = "12345678908",
            arbeidsgiverKid = null,
            mentorTimelonn = 500,
            mentorAntallTimer = 15.0
        )
        val vanligTilskudd = mentorTilskudd.copy(
            avtaleId = "vanlig-avtale",
            tiltakstype = Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD,
            deltakerEtternavn = "Vanlig",
            deltakerFornavn = "Vera",
            avtaleInnholdId = "v1",
            deltakerFnr = vanligFnr,
            tilskuddsperiodeId = "vanlig-periode",
            avtaleNr = 8882,
            lønnstilskuddsprosent = 60
        )

        val mentorRefusjon = refusjonService.opprettRefusjon(mentorTilskudd)!!
        val vanligRefusjon = refusjonService.opprettRefusjon(vanligTilskudd)!!

        val resultat = saksbehandler.finnAlle(
            HentSaksbehandlerRefusjonerQueryParametre(
                enhet = "1000",
                size = 50
            )
        )

        @Suppress("UNCHECKED_CAST")
        val liste = resultat["refusjoner"] as List<*>

        val harMentor = liste.any { it.toString().contains(mentorRefusjon.id) }
        val harVanlig = liste.any { it.toString().contains(vanligRefusjon.id) }

        assertThat(harVanlig).isTrue()
        assertThat(harMentor).isEqualTo(mentorToggle)
    }

}
