package no.nav.arbeidsgiver.tiltakrefusjon.inntekt

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import no.nav.arbeidsgiver.tiltakrefusjon.altinn.AltinnTilgangsstyringService
import no.nav.arbeidsgiver.tiltakrefusjon.altinn.Organisasjon
import no.nav.arbeidsgiver.tiltakrefusjon.autorisering.InnloggetArbeidsgiver
import no.nav.arbeidsgiver.tiltakrefusjon.etInntektsgrunnlag
import no.nav.arbeidsgiver.tiltakrefusjon.innloggetBruker
import no.nav.arbeidsgiver.tiltakrefusjon.persondata.PersondataService
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.*
import no.nav.arbeidsgiver.tiltakrefusjon.tilskuddsperiode.TilskuddsperiodeGodkjentMelding
import no.nav.arbeidsgiver.tiltakrefusjon.utils.Now
import no.nav.arbeidsgiver.tiltakrefusjon.utils.ulid
import no.nav.team_tiltak.felles.persondata.pdl.domene.Diskresjonskode
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDate
import java.time.LocalDateTime


@DirtiesContext
@SpringBootTest(properties = ["NAIS_APP_IMAGE=test", "tiltak-refusjon.inntektskomponenten.fake=false"])
@ActiveProfiles("local", "wiremock")
@AutoConfigureWireMock(port = 8090)
class RefusjonberegnerFratrekkFerieTest(
    @Autowired
    val refusjonService: RefusjonService,
    @Autowired
    val refusjonRepository: RefusjonRepository,
) {
    val innloggetArbeidsgiver = innloggetBruker("12345678910", BrukerRolle.ARBEIDSGIVER);

    @MockkBean
    lateinit var altinnTilgangsstyringService: AltinnTilgangsstyringService

    @MockkBean
    lateinit var korreksjonRepository: KorreksjonRepository

    @MockkBean
    lateinit var persondataService: PersondataService

    val WIREMOCK_IDENT: String = "08098613316"
    val WIREMOCK_VIRKSOMHET_IDENTIFIKATOR: String = "972674818"
    private val defaultOrg =
        Organisasjon("Bedrift AS", "Bedrift type", WIREMOCK_VIRKSOMHET_IDENTIFIKATOR, "Org form", "Status")

    fun lagEnTilskuddsperiodeGodkjentMelding(
        tilskuddFom: LocalDate,
        tilskuddTom: LocalDate,
        tiltakstype: Tiltakstype,
        tilskuddsbeløp: Int,
        deltakerFnr: String,
        bedriftNr: String
    ): TilskuddsperiodeGodkjentMelding {
        val tilskuddsperiodeGodkjentMelding = TilskuddsperiodeGodkjentMelding(
            avtaleId = ulid(),
            tilskuddsperiodeId = ulid(),
            avtaleInnholdId = ulid(),
            deltakerFornavn = "Henrik",
            deltakerEtternavn = "Wergeland",
            arbeidsgiverFornavn = "Arne",
            arbeidsgiverEtternavn = "Arbeidsgiver",
            arbeidsgiverTlf = "41111111",
            tiltakstype = tiltakstype,
            deltakerFnr = deltakerFnr,
            veilederNavIdent = "X123456",
            bedriftNavn = "Pengeløs sparebank AS",
            bedriftNr = bedriftNr,
            otpSats = 0.02,
            feriepengerSats = 0.12,
            arbeidsgiveravgiftSats = 0.141,
            lønnstilskuddsprosent = 40,
            tilskuddFom = tilskuddFom,
            tilskuddTom = tilskuddTom,
            tilskuddsbeløp = tilskuddsbeløp,
            avtaleNr = 1337,
            løpenummer = 9,
            resendingsnummer = null,
            enhet = "1104",
            godkjentTidspunkt = LocalDateTime.of(tilskuddTom.year, tilskuddTom.month, tilskuddTom.dayOfMonth, 0, 0)
        )
        return tilskuddsperiodeGodkjentMelding
    }

    fun opprettRefusjonOgGjørInntektoppslag(tilskuddsperiodeGodkjentMelding: TilskuddsperiodeGodkjentMelding): Refusjon {
        val refusjon = refusjonService.opprettRefusjon(tilskuddsperiodeGodkjentMelding) ?: throw Exception()
        refusjon.status = RefusjonStatus.KLAR_FOR_INNSENDING
        refusjon.unntakOmInntekterFremitid = 0
        refusjon.fristForGodkjenning = Now.localDate().plusDays(1)
        refusjonService.gjørBedriftKontonummeroppslag(refusjon)
        refusjonService.gjørInntektsoppslag(refusjon, innloggetArbeidsgiver)
        // Sett innhentede inntekter til opptjent i periode
        refusjon.refusjonsgrunnlag.inntektsgrunnlag?.inntekter?.filter { it.erMedIInntektsgrunnlag() }
            ?.forEach { it.erOpptjentIPeriode = true }
        // Bekreft at alle inntektene kun er fra tiltaket
        //refusjonService.oppdaterRefusjon(refusjon)
        refusjonService.endreBruttolønn(refusjon, true, null)
        refusjonService.gjørBeregning(refusjon, innloggetArbeidsgiver);

        return refusjon;
    }

    fun `vis utregning med feriefratrekk`(refusjon: Refusjon, TREKKFORFERIEGRUNNLAG: Int): Int {
        val beregning: Beregning = refusjon.refusjonsgrunnlag.beregning ?: throw Exception()
        val (lønn, _, feriepenger, tjenestepensjon, arbeidsgiveravgift) = beregning
        val resultatForAssert =
            ((lønn + TREKKFORFERIEGRUNNLAG + feriepenger + tjenestepensjon + arbeidsgiveravgift) * 0.40).toInt()
        return resultatForAssert
    }

    @BeforeEach
    fun slettAlt() {
        refusjonRepository.deleteAll()
    }

    @Test
    fun `hent inntektsoppslag som har feriefratrekk i måned som ikke er refusjonsmåned og beregn`() {
        val TREKKFORFERIEGRUNNLAG: Int = 0 // trekk grunnlag fra inntektoppslag

        val tilskuddsperiodeGodkjentMelding: TilskuddsperiodeGodkjentMelding = lagEnTilskuddsperiodeGodkjentMelding(
            LocalDate.of(2021, 7, 1),
            LocalDate.of(2021, 7, 31),
            Tiltakstype.SOMMERJOBB,
            60000,
            WIREMOCK_IDENT,
            WIREMOCK_VIRKSOMHET_IDENTIFIKATOR,
        )
        val refusjon = opprettRefusjonOgGjørInntektoppslag(tilskuddsperiodeGodkjentMelding)

        assert(
            refusjon.refusjonsgrunnlag.beregning!!.refusjonsbeløp == `vis utregning med feriefratrekk`(
                refusjon,
                TREKKFORFERIEGRUNNLAG
            )
        )
        assert(refusjon.refusjonsgrunnlag.beregning!!.fratrekkLønnFerie == TREKKFORFERIEGRUNNLAG)
    }

    @Disabled("Håndtering av ferietrekk med kun plussbeløp er ikke avklart")
    @Test
    fun `hent inntektsoppslag som har feriefratrekk i måned som er refusjonsmåned og beregn`() {
        val TREKKFORFERIEGRUNNLAG: Int = 7500 // trekk grunnlag fra inntektoppslag

        val tilskuddsperiodeGodkjentMelding: TilskuddsperiodeGodkjentMelding = lagEnTilskuddsperiodeGodkjentMelding(
            LocalDate.of(2022, 6, 1),
            LocalDate.of(2022, 6, 30),
            Tiltakstype.SOMMERJOBB,
            60000,
            WIREMOCK_IDENT,
            WIREMOCK_VIRKSOMHET_IDENTIFIKATOR,
        )
        val refusjon = opprettRefusjonOgGjørInntektoppslag(tilskuddsperiodeGodkjentMelding)

        assert(
            refusjon.refusjonsgrunnlag.beregning!!.refusjonsbeløp == `vis utregning med feriefratrekk`(
                refusjon,
                TREKKFORFERIEGRUNNLAG
            )
        )
        assert(refusjon.refusjonsgrunnlag.beregning!!.fratrekkLønnFerie == TREKKFORFERIEGRUNNLAG)
    }

    @Test
    fun `hent inntektsoppslag som ikke har feriefratrekk og beregn`() {
        val TREKKFORFERIEGRUNNLAG: Int = 0 // trekk grunnlag fra inntektoppslag

        val tilskuddsperiodeGodkjentMelding: TilskuddsperiodeGodkjentMelding = lagEnTilskuddsperiodeGodkjentMelding(
            LocalDate.of(2021, 6, 1),
            LocalDate.of(2021, 6, 30),
            Tiltakstype.SOMMERJOBB,
            60000,
            WIREMOCK_IDENT,
            WIREMOCK_VIRKSOMHET_IDENTIFIKATOR,
        )
        val refusjon = opprettRefusjonOgGjørInntektoppslag(tilskuddsperiodeGodkjentMelding)

        assert(
            refusjon.refusjonsgrunnlag.beregning!!.refusjonsbeløp == `vis utregning med feriefratrekk`(
                refusjon,
                TREKKFORFERIEGRUNNLAG
            )
        )
        assert(refusjon.refusjonsgrunnlag.beregning!!.fratrekkLønnFerie == TREKKFORFERIEGRUNNLAG)
    }

    @Test
    fun `hent inntektsoppslag som har minusbeløp på feriefratrekk og beregn`() {
        val TREKKFORFERIEGRUNNLAG: Int = -7500 // trekk grunnlag fra inntektoppslag

        val tilskuddsperiodeGodkjentMelding: TilskuddsperiodeGodkjentMelding = lagEnTilskuddsperiodeGodkjentMelding(
            LocalDate.of(2022, 6, 1),
            LocalDate.of(2022, 6, 30),
            Tiltakstype.SOMMERJOBB,
            60000,
            WIREMOCK_IDENT,
            WIREMOCK_VIRKSOMHET_IDENTIFIKATOR,
        )
        val refusjon = opprettRefusjonOgGjørInntektoppslag(tilskuddsperiodeGodkjentMelding)

        assert(
            refusjon.refusjonsgrunnlag.beregning!!.refusjonsbeløp == `vis utregning med feriefratrekk`(
                refusjon,
                TREKKFORFERIEGRUNNLAG
            )
        )
        assert(refusjon.refusjonsgrunnlag.beregning!!.fratrekkLønnFerie == TREKKFORFERIEGRUNNLAG)
    }

    @Test
    fun `gjør inntektsoppslag - både minus og pluss i trekkILoennForFerie`() {
        Now.fixedDate(LocalDate.of(2023, 7, 1))
        val TREKKFORFERIEGRUNNLAG1: Int = -7500 // trekk grunnlag fra inntektoppslag
        val TREKKFORFERIEGRUNNLAG2: Int = 5000 // trekk grunnlag fra inntektoppslag
        val fnrMedFerieTrekkIWireMock = "26089638754"

        val tilskuddsperiodeGodkjentMelding: TilskuddsperiodeGodkjentMelding = lagEnTilskuddsperiodeGodkjentMelding(
            tilskuddFom = LocalDate.of(2023, 6, 1),
            tilskuddTom = LocalDate.of(2023, 6, 30),
            tiltakstype = Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD,
            tilskuddsbeløp = 60000,
            deltakerFnr = fnrMedFerieTrekkIWireMock,
            bedriftNr = WIREMOCK_VIRKSOMHET_IDENTIFIKATOR,
        )
        val refusjon = opprettRefusjonOgGjørInntektoppslag(tilskuddsperiodeGodkjentMelding)

        val trekkLagtSammen = TREKKFORFERIEGRUNNLAG1 + TREKKFORFERIEGRUNNLAG2
        Assertions.assertEquals(
            `vis utregning med feriefratrekk`(refusjon, trekkLagtSammen),
            refusjon.refusjonsgrunnlag.beregning!!.refusjonsbeløp
        )
        Assertions.assertEquals(trekkLagtSammen, refusjon.refusjonsgrunnlag.beregning!!.fratrekkLønnFerie)
        Now.resetClock()
    }

    @Test() // Kan skje hvis arbeidsgiver skal korrigere/nedjustere tidligere a-melding
    fun `hent inntektsoppslag som har kun plussbeløp på feriefratrekk og beregn`() {
        Now.fixedDate(LocalDate.of(2023, 7, 1))
        val TREKKFORFERIEGRUNNLAG: Int = 5000 // trekk grunnlag fra inntektoppslag
        val fnrMedFerieTrekkIWireMock = "23039648083"

        val tilskuddsperiodeGodkjentMelding: TilskuddsperiodeGodkjentMelding = lagEnTilskuddsperiodeGodkjentMelding(
            tilskuddFom = LocalDate.of(2023, 6, 1),
            tilskuddTom = LocalDate.of(2023, 6, 30),
            tiltakstype = Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD,
            tilskuddsbeløp = 60000,
            deltakerFnr = fnrMedFerieTrekkIWireMock,
            bedriftNr = WIREMOCK_VIRKSOMHET_IDENTIFIKATOR,
        )
        val refusjon = opprettRefusjonOgGjørInntektoppslag(tilskuddsperiodeGodkjentMelding)

        val lønnFraWiremock = 60000
        val trekkiLoennForFerieWiremock = 5000
        assertThat(refusjon.refusjonsgrunnlag.beregning!!.lønn).isEqualTo(lønnFraWiremock)
        assertThat(refusjon.refusjonsgrunnlag.beregning!!.lønnFratrukketFerie).isEqualTo(lønnFraWiremock + trekkiLoennForFerieWiremock)
        assert(
            refusjon.refusjonsgrunnlag.beregning!!.refusjonsbeløp == `vis utregning med feriefratrekk`(
                refusjon,
                TREKKFORFERIEGRUNNLAG
            )
        )
        assert(refusjon.refusjonsgrunnlag.beregning!!.fratrekkLønnFerie == TREKKFORFERIEGRUNNLAG)
        Now.resetClock()
    }

    @Test
    fun `sjekk at leggSammenTrekkGrunnlag returnerer primiviteInt-eller-double`() {
        val etInntektsgrunnlag = etInntektsgrunnlag()
        val leggSammenTrekkGrunnlag: Double = leggSammenTrekkGrunnlag(
            etInntektsgrunnlag.inntekter.toList(),
            tilskuddFom = LocalDate.of(2021, 6, 1)
        )

        assertThat(leggSammenTrekkGrunnlag).isNotNull

    }

    @Test
    fun `trekk i lønn for ferie skal ikke trekkes på 2 refusjoner for samme måned`() {
        every { altinnTilgangsstyringService.altinnTilgangsstyringProperties.inntektsmeldingServiceCode } returns 4936
        every { altinnTilgangsstyringService.altinnTilgangsstyringProperties.inntektsmeldingServiceEdition } returns 1
        every {
            altinnTilgangsstyringService.hentInntektsmeldingTilganger(
                any()
            )
        } returns setOf<Organisasjon>(defaultOrg)
        every { altinnTilgangsstyringService.hentAdressesperreTilganger(any()) } returns setOf<Organisasjon>(defaultOrg)
        every { persondataService.hentDiskresjonskode(any()) } returns Diskresjonskode.UGRADERT
        val innloggetArbeidsgiver = InnloggetArbeidsgiver(
            "12345678901",
            altinnTilgangsstyringService,
            refusjonRepository,
            korreksjonRepository,
            refusjonService,
            persondataService
        )

        // Det kan oppstå 2 refusjoner innenfor samme måned ved f.eks. forlengelse. (eks. 01-15 og 16-30)
        //Now.fixedDate(LocalDate.of(2023, 7, 1))
        Now.fixedDateTime(LocalDateTime.of(2023, 7, 1, 0, 0, 0))
        val TREKKFORFERIEGRUNNLAG: Int = 5000 // trekk grunnlag fra inntektoppslag
        val fnrMedFerieTrekkIWireMock = "23039648083"

        val tilskuddsperiodeGodkjentMelding1: TilskuddsperiodeGodkjentMelding = lagEnTilskuddsperiodeGodkjentMelding(
            tilskuddFom = LocalDate.of(2023, 6, 1),
            tilskuddTom = LocalDate.of(2023, 6, 15),
            tiltakstype = Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD,
            tilskuddsbeløp = 60000,
            deltakerFnr = fnrMedFerieTrekkIWireMock,
            bedriftNr = WIREMOCK_VIRKSOMHET_IDENTIFIKATOR,
        )
        val tilskuddsperiodeGodkjentMelding2: TilskuddsperiodeGodkjentMelding = lagEnTilskuddsperiodeGodkjentMelding(
            tilskuddFom = LocalDate.of(2023, 6, 16),
            tilskuddTom = LocalDate.of(2023, 6, 30),
            tiltakstype = Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD,
            tilskuddsbeløp = 60000,
            deltakerFnr = fnrMedFerieTrekkIWireMock,
            bedriftNr = WIREMOCK_VIRKSOMHET_IDENTIFIKATOR,
        )


        val refusjon = opprettRefusjonOgGjørInntektoppslag(tilskuddsperiodeGodkjentMelding1)
        // Send inn
        refusjonService.godkjennForArbeidsgiver(refusjon, innloggetArbeidsgiver)
        assert(refusjon.refusjonsgrunnlag.beregning!!.fratrekkLønnFerie == TREKKFORFERIEGRUNNLAG)

        // Verifiser at ferietrekk ikke er med her
        val refusjon2 = opprettRefusjonOgGjørInntektoppslag(tilskuddsperiodeGodkjentMelding2)
        refusjonService.gjørBeregning(refusjon2, innloggetArbeidsgiver)
        refusjonRepository.save(refusjon2)
        Now.fixedDateTime(LocalDateTime.of(2023, 7, 1, 1, 0, 0))

        val refusjon2FraDb = innloggetArbeidsgiver.finnRefusjon(refusjon2.id)

        assertThat(refusjon2FraDb.refusjonsgrunnlag.beregning!!.fratrekkLønnFerie).isEqualTo(0)

        Now.resetClock()
    }

    @Test
    fun `feil med feriepenger_FAGSYSTEM-339222`() {
        every { altinnTilgangsstyringService.altinnTilgangsstyringProperties.inntektsmeldingServiceCode } returns 4936
        every { altinnTilgangsstyringService.altinnTilgangsstyringProperties.inntektsmeldingServiceEdition } returns 1
        every {
            altinnTilgangsstyringService.hentInntektsmeldingTilganger(
                any()
            )
        } returns setOf<Organisasjon>(defaultOrg)
        every { altinnTilgangsstyringService.hentAdressesperreTilganger(any()) } returns setOf(defaultOrg)
        val innloggetArbeidsgiver = InnloggetArbeidsgiver(
            "12345678901",
            altinnTilgangsstyringService,
            refusjonRepository,
            korreksjonRepository,
            refusjonService,
            persondataService
        )

        Now.fixedDateTime(LocalDateTime.of(2024, 7, 1, 0, 0, 0))
        val fnrMedFerieTrekkIWireMock = "29047497068"

        val tilskuddsperiodeGodkjentMelding1: TilskuddsperiodeGodkjentMelding = lagEnTilskuddsperiodeGodkjentMelding(
            tilskuddFom = LocalDate.of(2024, 6, 1),
            tilskuddTom = LocalDate.of(2024, 6, 15),
            tiltakstype = Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD,
            tilskuddsbeløp = 60000,
            deltakerFnr = fnrMedFerieTrekkIWireMock,
            bedriftNr = WIREMOCK_VIRKSOMHET_IDENTIFIKATOR,
        )
        val tilskuddsperiodeGodkjentMelding2: TilskuddsperiodeGodkjentMelding = lagEnTilskuddsperiodeGodkjentMelding(
            tilskuddFom = LocalDate.of(2024, 6, 16),
            tilskuddTom = LocalDate.of(2024, 6, 30),
            tiltakstype = Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD,
            tilskuddsbeløp = 60000,
            deltakerFnr = fnrMedFerieTrekkIWireMock,
            bedriftNr = WIREMOCK_VIRKSOMHET_IDENTIFIKATOR,
        )
        val refusjon = opprettRefusjonOgGjørInntektoppslag(tilskuddsperiodeGodkjentMelding1)
        // Send inn
        refusjonService.godkjennForArbeidsgiver(refusjon, innloggetArbeidsgiver)
        assertEquals(-52700, refusjon.refusjonsgrunnlag.beregning!!.fratrekkLønnFerie)
        assertEquals(45863, refusjon.refusjonsgrunnlag.beregning!!.lønn)
        assertEquals(-3565, refusjon.refusjonsgrunnlag.beregning!!.refusjonsbeløp)
        Now.resetClock()
    }
}
