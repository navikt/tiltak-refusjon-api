package no.nav.arbeidsgiver.tiltakrefusjon.autorisering

import com.ninjasquad.springmockk.MockkBean
import com.ninjasquad.springmockk.SpykBean
import io.mockk.every
import no.nav.arbeidsgiver.tiltakrefusjon.altinn.AltinnTilgangsstyringService
import no.nav.arbeidsgiver.tiltakrefusjon.altinn.Organisasjon
import no.nav.arbeidsgiver.tiltakrefusjon.innloggetBruker
import no.nav.arbeidsgiver.tiltakrefusjon.inntekt.InntektskomponentService
import no.nav.arbeidsgiver.tiltakrefusjon.persondata.PersondataService
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.*
import no.nav.arbeidsgiver.tiltakrefusjon.tilskuddsperiode.TilskuddsperiodeGodkjentMelding
import no.nav.arbeidsgiver.tiltakrefusjon.utils.Now
import no.nav.arbeidsgiver.tiltakrefusjon.varsling.VarslingRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDateTime
import java.time.temporal.TemporalAdjusters
import no.nav.team_tiltak.felles.persondata.pdl.domene.Diskresjonskode
import org.junit.jupiter.api.assertThrows


@SpringBootTest(properties = ["NAIS_APP_IMAGE=test"])
@ActiveProfiles("local")
@AutoConfigureWireMock(port = 8090)
internal class InnloggetArbeidsgiverTest(
    @Autowired
    val refusjonService: RefusjonService,
    @Autowired
    val refusjonRepository: RefusjonRepository,
    @Autowired
    val varslingRepository: VarslingRepository,
    @Autowired
    val minusbelopRepository: MinusbelopRepository,
) {
    val innloggetArbeidsgiverBruker = innloggetBruker(
        "16120102137",
        BrukerRolle.ARBEIDSGIVER
    )

    @SpykBean
    lateinit var inntektskomponentService: InntektskomponentService
    @MockkBean lateinit var altinnTilgangsstyringService: AltinnTilgangsstyringService
    @MockkBean lateinit var persondataService: PersondataService
    @MockkBean lateinit var korreksjonRepository: KorreksjonRepository
    @BeforeEach
    fun setup() {
        varslingRepository.deleteAll()
        refusjonRepository.deleteAll()
        minusbelopRepository.deleteAll()
        val organisasjon: Organisasjon = Organisasjon(
            "Bedrift AS",
            "Bedrift type",
            "999999999",
            "Org form",
            "Status"
        )
        every { altinnTilgangsstyringService.altinnTilgangsstyringProperties.serviceCode } returns 4936
        every { altinnTilgangsstyringService.altinnTilgangsstyringProperties.serviceEdition } returns 1
        every { persondataService.hentDiskresjonskode(any()) } returns Diskresjonskode.UGRADERT
        every { altinnTilgangsstyringService.hentAdressesperreTilganger(any())} returns setOf<Organisasjon>(
            organisasjon
        )
        every { altinnTilgangsstyringService.hentTilganger(any(), any(), any())  } returns setOf<Organisasjon>(
            organisasjon
        )
    }
    @Test
    fun finnRefusjon() {
        val deltakerFnr = "00000000000"
        val tilskuddMelding = TilskuddsperiodeGodkjentMelding(
            avtaleId = "1",
            tilskuddsbeløp = 1000,
            tiltakstype = Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD,
            deltakerEtternavn = "Mus",
            deltakerFornavn = "Mikke",
            arbeidsgiverFornavn = "Arne",
            arbeidsgiverEtternavn = "Arbeidsgiver",
            arbeidsgiverTlf = "41111111",
            arbeidsgiveravgiftSats = 0.141,
            avtaleInnholdId = "1",
            bedriftNavn = "Bedriften AS",
            bedriftNr = "999999999",
            deltakerFnr = deltakerFnr,
            feriepengerSats = 0.125,
            otpSats = 0.03,
            tilskuddFom =  Now.localDate().minusWeeks(4),
            tilskuddTom = Now.localDate().minusDays(1),
            tilskuddsperiodeId = "1",
            veilederNavIdent = "X123456",
            lønnstilskuddsprosent = 60,
            avtaleNr = 3456,
            løpenummer = 1,
            resendingsnummer = null,
            enhet = "1000",
            godkjentTidspunkt = LocalDateTime.now()
        )
        val tilskuddMelding2LittEldreMedLøpenummer2 = TilskuddsperiodeGodkjentMelding(
            avtaleId = "1",
            tilskuddsbeløp = 1000,
            tiltakstype = Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD,
            deltakerEtternavn = "Mus",
            deltakerFornavn = "Mikke",
            arbeidsgiverFornavn = "Arne",
            arbeidsgiverEtternavn = "Arbeidsgiver",
            arbeidsgiverTlf = "41111111",
            arbeidsgiveravgiftSats = 0.141,
            avtaleInnholdId = "2",
            bedriftNavn = "Bedriften AS",
            bedriftNr = "999999999",
            deltakerFnr = deltakerFnr,
            feriepengerSats = 0.125,
            otpSats = 0.03,
            tilskuddFom = Now.localDate().minusWeeks(3),
            tilskuddTom = Now.localDate().minusDays(1),
            tilskuddsperiodeId = "2",
            veilederNavIdent = "X123456",
            lønnstilskuddsprosent = 60,
            avtaleNr = 3456,
            løpenummer = 2,
            resendingsnummer = null,
            enhet = "1000",
            godkjentTidspunkt = LocalDateTime.now()
        )

        val refusjon1 = refusjonService.opprettRefusjon(tilskuddMelding)!!
        val refusjon2 = refusjonService.opprettRefusjon(tilskuddMelding2LittEldreMedLøpenummer2)!!

        val innloggetArbeidsgiver = InnloggetArbeidsgiver("16120102137",altinnTilgangsstyringService,refusjonRepository,korreksjonRepository,refusjonService, persondataService)
        val refusjonFunnet = innloggetArbeidsgiver.finnRefusjon(refusjon1.id)
        assertThat(refusjonFunnet).isEqualTo(refusjon1)

        val refusjonFunnet2 = innloggetArbeidsgiver.finnRefusjon(refusjon2.id)
        assertThat(refusjonFunnet2).isEqualTo(refusjon2)
    }

    @Test
    fun lagEnRefusjonMedMinusbeløp() {
        val deltakerFnr = "08098613316"
        val periode2start = Now.localDate().minusMonths(3).with(TemporalAdjusters.firstDayOfMonth());
        val periode2slutt = Now.localDate().minusMonths(3).with(TemporalAdjusters.lastDayOfMonth());
        val innloggetArbeidsgiver = InnloggetArbeidsgiver(
            "16120102137",
            altinnTilgangsstyringService,
            refusjonRepository,
            korreksjonRepository,
            refusjonService,
            persondataService
        )

        val tilskuddMelding2LittEldreMedLøpenummer2 = TilskuddsperiodeGodkjentMelding(
            avtaleId = "1",
            tilskuddsbeløp = 1000,
            tiltakstype = Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD,
            deltakerEtternavn = "Mus",
            deltakerFornavn = "Mikke",
            arbeidsgiverFornavn = "Arne",
            arbeidsgiverEtternavn = "Arbeidsgiver",
            arbeidsgiverTlf = "41111111",
            arbeidsgiveravgiftSats = 0.141,
            avtaleInnholdId = "2",
            bedriftNavn = "Bedriften AS",
            bedriftNr = "999999999",
            deltakerFnr = deltakerFnr,
            feriepengerSats = 0.125,
            otpSats = 0.03,
            tilskuddFom = periode2start,
            tilskuddTom = periode2slutt,
            tilskuddsperiodeId = "2",
            veilederNavIdent = "X123456",
            lønnstilskuddsprosent = 60,
            avtaleNr = 3456,
            løpenummer = 2,
            resendingsnummer = null,
            enhet = "1000",
            godkjentTidspunkt = LocalDateTime.now()
        )

        val refusjon2 = opprettRefusjonOgGjørInntektoppslag(tilskuddMelding2LittEldreMedLøpenummer2)
        // Skal ikke ha minus fra gammel refusjon, men få minus fra ferietrekk
        val refusjon2ById = innloggetArbeidsgiver.finnRefusjon(refusjon2.id)
        refusjon2ById.refusjonsgrunnlag.inntektsgrunnlag?.inntekter?.filter { it.erMedIInntektsgrunnlag() }
            ?.forEach { it.erOpptjentIPeriode = true }
        refusjonService.godkjennForArbeidsgiver(refusjon2ById, innloggetArbeidsgiver)
        val refusjonMedMinus = innloggetArbeidsgiver.finnRefusjon(refusjon2.id)
        assertThat(refusjonMedMinus.minusbelop).isNotNull
    }

    @Test
    fun finnRefusjonMedMinusBeløpFraTidligereRefusjon(){
        val deltakerFnr = "08098613316"

        val periode1start = Now.localDate().minusMonths(4).with(TemporalAdjusters.firstDayOfMonth());
        val periode1slutt = Now.localDate().minusMonths(4).with(TemporalAdjusters.lastDayOfMonth());
        val periode2start = Now.localDate().minusMonths(3).with(TemporalAdjusters.firstDayOfMonth());
        val periode2slutt = Now.localDate().minusMonths(3).with(TemporalAdjusters.lastDayOfMonth());
        val periode3start = Now.localDate().minusMonths(2).with(TemporalAdjusters.firstDayOfMonth());
        val periode3slutt = Now.localDate().minusMonths(2).with(TemporalAdjusters.lastDayOfMonth());
        val periode4start = Now.localDate().minusMonths(1).with(TemporalAdjusters.firstDayOfMonth());
        val periode4slutt = Now.localDate().minusMonths(1).with(TemporalAdjusters.lastDayOfMonth());

        val tilskuddMelding = TilskuddsperiodeGodkjentMelding(
            avtaleId = "1",
            tilskuddsbeløp = 1000,
            tiltakstype = Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD,
            deltakerEtternavn = "Mus",
            deltakerFornavn = "Mikke",
            arbeidsgiverFornavn = "Arne",
            arbeidsgiverEtternavn = "Arbeidsgiver",
            arbeidsgiverTlf = "41111111",
            arbeidsgiveravgiftSats = 0.141,
            avtaleInnholdId = "1",
            bedriftNavn = "Bedriften AS",
            bedriftNr = "999999999",
            deltakerFnr = deltakerFnr,
            feriepengerSats = 0.125,
            otpSats = 0.03,
            tilskuddFom = periode1start,
            tilskuddTom = periode1slutt,
            tilskuddsperiodeId = "1",
            veilederNavIdent = "X123456",
            lønnstilskuddsprosent = 60,
            avtaleNr = 3456,
            løpenummer = 1,
            resendingsnummer = null,
            enhet = "1000",
            godkjentTidspunkt = LocalDateTime.now()
        )
        val tilskuddMelding2LittEldreMedLøpenummer2 = TilskuddsperiodeGodkjentMelding(
            avtaleId = "1",
            tilskuddsbeløp = 1000,
            tiltakstype = Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD,
            deltakerEtternavn = "Mus",
            deltakerFornavn = "Mikke",
            arbeidsgiverFornavn = "Arne",
            arbeidsgiverEtternavn = "Arbeidsgiver",
            arbeidsgiverTlf = "41111111",
            arbeidsgiveravgiftSats = 0.141,
            avtaleInnholdId = "2",
            bedriftNavn = "Bedriften AS",
            bedriftNr = "999999999",
            deltakerFnr = deltakerFnr,
            feriepengerSats = 0.125,
            otpSats = 0.03,
            tilskuddFom = periode2start,
            tilskuddTom = periode2slutt,
            tilskuddsperiodeId = "2",
            veilederNavIdent = "X123456",
            lønnstilskuddsprosent = 60,
            avtaleNr = 3456,
            løpenummer = 2,
            resendingsnummer = null,
            enhet = "1000",
            godkjentTidspunkt = LocalDateTime.now()
        )

        // Stort nok tilskuddsbeløp for å "nullstille" siste minusbeløp
        val tilskuddMelding3LittEldreMedLøpenummer3 = TilskuddsperiodeGodkjentMelding(
            avtaleId = "1",
            tilskuddsbeløp = 10000,
            tiltakstype = Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD,
            deltakerEtternavn = "Mus",
            deltakerFornavn = "Mikke",
            arbeidsgiverFornavn = "Arne",
            arbeidsgiverEtternavn = "Arbeidsgiver",
            arbeidsgiverTlf = "41111111",
            arbeidsgiveravgiftSats = 0.141,
            avtaleInnholdId = "2",
            bedriftNavn = "Bedriften AS",
            bedriftNr = "999999999",
            deltakerFnr = deltakerFnr,
            feriepengerSats = 0.125,
            otpSats = 0.03,
            tilskuddFom = periode3start,
            tilskuddTom = periode3slutt,
            tilskuddsperiodeId = "3",
            veilederNavIdent = "X123456",
            lønnstilskuddsprosent = 60,
            avtaleNr = 3456,
            løpenummer = 3,
            resendingsnummer = null,
            enhet = "1000",
            godkjentTidspunkt = LocalDateTime.now()
        )

        val tilskuddMelding4LittEldreMedLøpenummer4 = TilskuddsperiodeGodkjentMelding(
            avtaleId = "1",
            tilskuddsbeløp = 10000,
            tiltakstype = Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD,
            deltakerEtternavn = "Mus",
            deltakerFornavn = "Mikke",
            arbeidsgiverFornavn = "Arne",
            arbeidsgiverEtternavn = "Arbeidsgiver",
            arbeidsgiverTlf = "41111111",
            arbeidsgiveravgiftSats = 0.141,
            avtaleInnholdId = "2",
            bedriftNavn = "Bedriften AS",
            bedriftNr = "999999999",
            deltakerFnr = deltakerFnr,
            feriepengerSats = 0.125,
            otpSats = 0.03,
            tilskuddFom = periode4start,
            tilskuddTom = periode4slutt,
            tilskuddsperiodeId = "4",
            veilederNavIdent = "X123456",
            lønnstilskuddsprosent = 60,
            avtaleNr = 3456,
            løpenummer = 4,
            resendingsnummer = null,
            enhet = "1000",
            godkjentTidspunkt = LocalDateTime.now()
        )

        val innloggetArbeidsgiver = InnloggetArbeidsgiver("16120102137",altinnTilgangsstyringService,refusjonRepository,korreksjonRepository,refusjonService, persondataService)

        // Tre refusjoner med samme avtalenr.
        val refusjon1 = opprettRefusjonOgGjørInntektoppslag(tilskuddMelding)
        val refusjon2 = opprettRefusjonOgGjørInntektoppslag(tilskuddMelding2LittEldreMedLøpenummer2)
        val refusjon3 = opprettRefusjonOgGjørInntektoppslag(tilskuddMelding3LittEldreMedLøpenummer3)
        val refusjon4 = opprettRefusjonOgGjørInntektoppslag(tilskuddMelding4LittEldreMedLøpenummer4)

        // Skal ikke ha noe minus fra gammel refusjon eller inntekt
        val refusjon1FunnetViaFinnRefusjon = innloggetArbeidsgiver.finnRefusjon(refusjon1.id)
        assertThat(refusjon1).isEqualTo(refusjon1FunnetViaFinnRefusjon)
        // Sett innhentede inntekter til opptjent i periode
        refusjon1FunnetViaFinnRefusjon.refusjonsgrunnlag.inntektsgrunnlag?.inntekter?.filter { it.erMedIInntektsgrunnlag() }?.forEach { it.erOpptjentIPeriode = true }
        assertThat(refusjon1FunnetViaFinnRefusjon.refusjonsgrunnlag.forrigeRefusjonMinusBeløp).isEqualTo(0)
        refusjonService.godkjennForArbeidsgiver(refusjon1FunnetViaFinnRefusjon, innloggetArbeidsgiver)

        // Skal ikke ha minus fra gammel refusjon, men få minus fra ferietrekk
        val refusjon2FunnetViaFinnRefusjon = innloggetArbeidsgiver.finnRefusjon(refusjon2.id)
        refusjon2FunnetViaFinnRefusjon.refusjonsgrunnlag.inntektsgrunnlag?.inntekter?.filter { it.erMedIInntektsgrunnlag() }
            ?.forEach { it.erOpptjentIPeriode = true }
        refusjonService.godkjennForArbeidsgiver(refusjon2FunnetViaFinnRefusjon, innloggetArbeidsgiver)

        // Skal finne gammel minus, men ikke minus fra inntekt
        val refusjon3FunnetViaFinnRefusjon = innloggetArbeidsgiver.finnRefusjon(refusjon3.id)
        refusjon3FunnetViaFinnRefusjon.refusjonsgrunnlag.inntektsgrunnlag?.inntekter?.filter { it.erMedIInntektsgrunnlag() }?.forEach { it.erOpptjentIPeriode = true }
        assertThat(refusjon3FunnetViaFinnRefusjon.refusjonsgrunnlag.forrigeRefusjonMinusBeløp).isEqualTo(-3966)
        assertThat(refusjon3FunnetViaFinnRefusjon.refusjonsgrunnlag.beregning!!.refusjonsbeløp).isPositive()
        refusjonService.godkjennForArbeidsgiver(refusjon3FunnetViaFinnRefusjon, innloggetArbeidsgiver)

        // Minus skal nå være nullstillt
        val refusjon4FunnetViaFinnRefusjon = innloggetArbeidsgiver.finnRefusjon(refusjon4.id)
        refusjon4FunnetViaFinnRefusjon.refusjonsgrunnlag.inntektsgrunnlag?.inntekter?.filter { it.erMedIInntektsgrunnlag() }?.forEach { it.erOpptjentIPeriode = true }
        assertThat(refusjon4FunnetViaFinnRefusjon.refusjonsgrunnlag.forrigeRefusjonMinusBeløp).isEqualTo(0)
        assertThat(refusjon4FunnetViaFinnRefusjon.refusjonsgrunnlag.beregning!!.refusjonsbeløp).isPositive()
        refusjonService.godkjennForArbeidsgiver(refusjon4FunnetViaFinnRefusjon, innloggetArbeidsgiver)
    }

    @Test
    fun finnRefusjonMedMinusBeløpFraForrigeRefusjonSidenDetErUlikAvtaleNrSkalMinusBeløpetIkkeTasMedIAndreRefusjoner(){
        val deltakerFnr = "08098613316"

        val periode1start = Now.localDate().minusMonths(4).with(TemporalAdjusters.firstDayOfMonth());
        val periode1slutt = Now.localDate().minusMonths(4).with(TemporalAdjusters.lastDayOfMonth());
        val periode2start = Now.localDate().minusMonths(3).with(TemporalAdjusters.firstDayOfMonth());
        val periode2slutt = Now.localDate().minusMonths(3).with(TemporalAdjusters.lastDayOfMonth());
        val periode3start = Now.localDate().minusMonths(2).with(TemporalAdjusters.firstDayOfMonth());
        val periode3slutt = Now.localDate().minusMonths(2).with(TemporalAdjusters.lastDayOfMonth());
        val periode4start = Now.localDate().minusMonths(1).with(TemporalAdjusters.firstDayOfMonth());
        val periode4slutt = Now.localDate().minusMonths(1).with(TemporalAdjusters.lastDayOfMonth());

        val tilskuddMelding = TilskuddsperiodeGodkjentMelding(
            avtaleId = "1",
            tilskuddsbeløp = 1000,
            tiltakstype = Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD,
            deltakerEtternavn = "Mus",
            deltakerFornavn = "Mikke",
            arbeidsgiverFornavn = "Arne",
            arbeidsgiverEtternavn = "Arbeidsgiver",
            arbeidsgiverTlf = "41111111",
            arbeidsgiveravgiftSats = 0.141,
            avtaleInnholdId = "1",
            bedriftNavn = "Bedriften AS",
            bedriftNr = "999999999",
            deltakerFnr = deltakerFnr,
            feriepengerSats = 0.125,
            otpSats = 0.03,
            tilskuddFom = periode1start,
            tilskuddTom = periode1slutt,
            tilskuddsperiodeId = "1",
            veilederNavIdent = "X123456",
            lønnstilskuddsprosent = 60,
            avtaleNr = 3456,
            løpenummer = 1,
            resendingsnummer = null,
            enhet = "1000",
            godkjentTidspunkt = LocalDateTime.now()
        )
        val tilskuddMelding2LittEldreMedLøpenummer2 = TilskuddsperiodeGodkjentMelding(
            avtaleId = "2",
            tilskuddsbeløp = 1000,
            tiltakstype = Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD,
            deltakerEtternavn = "Mus",
            deltakerFornavn = "Mikke",
            arbeidsgiverFornavn = "Arne",
            arbeidsgiverEtternavn = "Arbeidsgiver",
            arbeidsgiverTlf = "41111111",
            arbeidsgiveravgiftSats = 0.141,
            avtaleInnholdId = "2",
            bedriftNavn = "Bedriften AS",
            bedriftNr = "999999999",
            deltakerFnr = deltakerFnr,
            feriepengerSats = 0.125,
            otpSats = 0.03,
            tilskuddFom = periode2start,
            tilskuddTom = periode2slutt,
            tilskuddsperiodeId = "2",
            veilederNavIdent = "X123456",
            lønnstilskuddsprosent = 60,
            avtaleNr = 5678,
            løpenummer = 2,
            resendingsnummer = null,
            enhet = "1000",
            godkjentTidspunkt = LocalDateTime.now()
        )

        // Stort nok tilskuddsbeløp for å "nullstille" siste minusbeløp
        val tilskuddMelding3LittEldreMedLøpenummer3 = TilskuddsperiodeGodkjentMelding(
            avtaleId = "3",
            tilskuddsbeløp = 10000,
            tiltakstype = Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD,
            deltakerEtternavn = "Mus",
            deltakerFornavn = "Mikke",
            arbeidsgiverFornavn = "Arne",
            arbeidsgiverEtternavn = "Arbeidsgiver",
            arbeidsgiverTlf = "41111111",
            arbeidsgiveravgiftSats = 0.141,
            avtaleInnholdId = "2",
            bedriftNavn = "Bedriften AS",
            bedriftNr = "999999999",
            deltakerFnr = deltakerFnr,
            feriepengerSats = 0.125,
            otpSats = 0.03,
            tilskuddFom = periode3start,
            tilskuddTom = periode3slutt,
            tilskuddsperiodeId = "3",
            veilederNavIdent = "X123456",
            lønnstilskuddsprosent = 60,
            avtaleNr = 9012,
            løpenummer = 3,
            resendingsnummer = null,
            enhet = "1000",
            godkjentTidspunkt = LocalDateTime.now()
        )

        // Stort nok tilskuddsbeløp for å "nullstille" siste minusbeløp
        val tilskuddMelding4LittEldreMedLøpenummer4 = TilskuddsperiodeGodkjentMelding(
            avtaleId = "4",
            tilskuddsbeløp = 10000,
            tiltakstype = Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD,
            deltakerEtternavn = "Mus",
            deltakerFornavn = "Mikke",
            arbeidsgiverFornavn = "Arne",
            arbeidsgiverEtternavn = "Arbeidsgiver",
            arbeidsgiverTlf = "41111111",
            arbeidsgiveravgiftSats = 0.141,
            avtaleInnholdId = "2",
            bedriftNavn = "Bedriften AS",
            bedriftNr = "999999999",
            deltakerFnr = deltakerFnr,
            feriepengerSats = 0.125,
            otpSats = 0.03,
            tilskuddFom = periode3start,
            tilskuddTom = periode3slutt,
            tilskuddsperiodeId = "4",
            veilederNavIdent = "X123456",
            lønnstilskuddsprosent = 60,
            avtaleNr = 12445532,
            løpenummer = 3,
            resendingsnummer = null,
            enhet = "1000",
            godkjentTidspunkt = LocalDateTime.now()
        )

        val innloggetArbeidsgiver = InnloggetArbeidsgiver("16120102137",altinnTilgangsstyringService,refusjonRepository,korreksjonRepository,refusjonService, persondataService)

        // Tre refusjoner med ulik avtalenr
        val refusjon1 = opprettRefusjonOgGjørInntektoppslag(tilskuddMelding)!!
        val refusjon2 = opprettRefusjonOgGjørInntektoppslag(tilskuddMelding2LittEldreMedLøpenummer2)!!
        val refusjon3 = opprettRefusjonOgGjørInntektoppslag(tilskuddMelding3LittEldreMedLøpenummer3)!!
        val refusjon4 = opprettRefusjonOgGjørInntektoppslag(tilskuddMelding4LittEldreMedLøpenummer4)!!

        val refusjon1FunnetViaFinnRefusjon = innloggetArbeidsgiver.finnRefusjon(refusjon1.id)
        assertThat(refusjon1FunnetViaFinnRefusjon.refusjonsgrunnlag.forrigeRefusjonMinusBeløp).isEqualTo(0)
        refusjonService.godkjennForArbeidsgiver(refusjon1FunnetViaFinnRefusjon, innloggetArbeidsgiver)

        val refusjon2FunnetViaFinnRefusjon = innloggetArbeidsgiver.finnRefusjon(refusjon2.id)
        assertThat(refusjon2FunnetViaFinnRefusjon.refusjonsgrunnlag.forrigeRefusjonMinusBeløp).isEqualTo(0)
        refusjonService.godkjennForArbeidsgiver(refusjon2, innloggetArbeidsgiver)


        val refusjon3FunnetViaFinnRefusjon = innloggetArbeidsgiver.finnRefusjon(refusjon3.id)
        assertThat(refusjon3FunnetViaFinnRefusjon.refusjonsgrunnlag.forrigeRefusjonMinusBeløp).isEqualTo(0)
        refusjonService.godkjennForArbeidsgiver(refusjon3FunnetViaFinnRefusjon, innloggetArbeidsgiver)

        val refusjon4unnetViaFinnRefusjon = innloggetArbeidsgiver.finnRefusjon(refusjon4.id)
        assertThat(refusjon4unnetViaFinnRefusjon.refusjonsgrunnlag.forrigeRefusjonMinusBeløp).isEqualTo(0)
        refusjonService.godkjennForArbeidsgiver(refusjon4unnetViaFinnRefusjon, innloggetArbeidsgiver)


    }

    @Test
    fun finnRefusjonUtenMinusBeløpSidenDetErBareEnRefusjonMedLøpenummer1(){
        val tilskuddMeldingUtenFerieTrekk = TilskuddsperiodeGodkjentMelding(
            avtaleId = "1",
            tilskuddsbeløp = 1000,
            tiltakstype = Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD,
            deltakerEtternavn = "Mus",
            deltakerFornavn = "Mikke",
            arbeidsgiverFornavn = "Arne",
            arbeidsgiverEtternavn = "Arbeidsgiver",
            arbeidsgiverTlf = "41111111",
            arbeidsgiveravgiftSats = 0.141,
            avtaleInnholdId = "1",
            bedriftNavn = "Bedriften AS",
            bedriftNr = "999999999",
            deltakerFnr = "01092211111",
            feriepengerSats = 0.125,
            otpSats = 0.03,
            tilskuddFom =  Now.localDate().minusWeeks(4),
            tilskuddTom = Now.localDate().minusDays(1),
            tilskuddsperiodeId = "1",
            veilederNavIdent = "X123456",
            lønnstilskuddsprosent = 60,
            avtaleNr = 3456,
            løpenummer = 1,
            resendingsnummer = null,
            enhet = "1000",
            godkjentTidspunkt = LocalDateTime.now()
        )


        val refusjon1 = opprettRefusjonOgGjørInntektoppslag(tilskuddMeldingUtenFerieTrekk)!!
        refusjonService.godkjennForArbeidsgiver(refusjon1, innloggetArbeidsgiverBruker)


        val innloggetArbeidsgiver = InnloggetArbeidsgiver("16120102137",altinnTilgangsstyringService,refusjonRepository,korreksjonRepository,refusjonService, persondataService)

        val refusjon1FunnetViaFinnRefusjon = innloggetArbeidsgiver.finnRefusjon(refusjon1.id)

        assertThat(refusjon1FunnetViaFinnRefusjon.refusjonsgrunnlag.forrigeRefusjonMinusBeløp).isEqualTo(0)
        assertThat(refusjon1FunnetViaFinnRefusjon.refusjonsgrunnlag.beregning!!.refusjonsbeløp).isGreaterThan(0)
    }

    @Test
    fun `refusjon som gjør opp minusbeløp blir lagret`() {
        val periode2start = Now.localDate().minusMonths(3).with(TemporalAdjusters.firstDayOfMonth());
        val periode2slutt = Now.localDate().minusMonths(3).with(TemporalAdjusters.lastDayOfMonth());
        val periode3start = Now.localDate().minusMonths(2).with(TemporalAdjusters.firstDayOfMonth());
        val periode3slutt = Now.localDate().minusMonths(2).with(TemporalAdjusters.lastDayOfMonth());

        val deltakerFnr = "08098613316"
        val tilskuddMelding2LittEldreMedLøpenummer2 = TilskuddsperiodeGodkjentMelding(
            avtaleId = "1",
            tilskuddsbeløp = 1000,
            tiltakstype = Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD,
            deltakerEtternavn = "Mus",
            deltakerFornavn = "Mikke",
            arbeidsgiverFornavn = "Arne",
            arbeidsgiverEtternavn = "Arbeidsgiver",
            arbeidsgiverTlf = "41111111",
            arbeidsgiveravgiftSats = 0.141,
            avtaleInnholdId = "2",
            bedriftNavn = "Bedriften AS",
            bedriftNr = "999999999",
            deltakerFnr = deltakerFnr,
            feriepengerSats = 0.125,
            otpSats = 0.03,
            tilskuddFom = periode2start,
            tilskuddTom = periode2slutt,
            tilskuddsperiodeId = "2",
            veilederNavIdent = "X123456",
            lønnstilskuddsprosent = 60,
            avtaleNr = 3456,
            løpenummer = 2,
            resendingsnummer = null,
            enhet = "1000",
            godkjentTidspunkt = LocalDateTime.now()
        )

        // Stort nok tilskuddsbeløp for å "nullstille" siste minusbeløp
        val tilskuddMelding3LittEldreMedLøpenummer3 = tilskuddMelding2LittEldreMedLøpenummer2.copy(
            tilskuddsbeløp = 10000,
            tilskuddFom = periode3start,
            tilskuddTom = periode3slutt,
            tilskuddsperiodeId = "3",
            løpenummer = 3
        )

        val innloggetArbeidsgiver = InnloggetArbeidsgiver("16120102137",altinnTilgangsstyringService,refusjonRepository,korreksjonRepository,refusjonService, persondataService)

        val refusjon2 = opprettRefusjonOgGjørInntektoppslag(tilskuddMelding2LittEldreMedLøpenummer2)
        val refusjon3 = opprettRefusjonOgGjørInntektoppslag(tilskuddMelding3LittEldreMedLøpenummer3)

        // Skal ikke ha minus fra gammel refusjon, men få minus fra ferietrekk
        val refusjon2FunnetViaFinnRefusjon = innloggetArbeidsgiver.finnRefusjon(refusjon2.id)
        refusjon2FunnetViaFinnRefusjon.refusjonsgrunnlag.inntektsgrunnlag?.inntekter?.filter { it.erMedIInntektsgrunnlag() }
            ?.forEach { it.erOpptjentIPeriode = true }
        refusjonService.godkjennForArbeidsgiver(refusjon2FunnetViaFinnRefusjon, innloggetArbeidsgiver)

        val minusbeløpRefusjon2 =
            minusbelopRepository.findAllByAvtaleNr(refusjon2.refusjonsgrunnlag.tilskuddsgrunnlag.avtaleNr)
        assertThat(minusbeløpRefusjon2.size).isEqualTo(1)
        assertThat(minusbeløpRefusjon2.first().gjortOpp).isFalse()
        assertThat(minusbeløpRefusjon2.first().gjortOppAvRefusjonId).isNull()


        // Skal finne gammel minus, men ikke minus fra inntekt
        val refusjon3FunnetViaFinnRefusjon = innloggetArbeidsgiver.finnRefusjon(refusjon3.id)
        refusjon3FunnetViaFinnRefusjon.refusjonsgrunnlag.inntektsgrunnlag?.inntekter?.filter { it.erMedIInntektsgrunnlag() }?.forEach { it.erOpptjentIPeriode = true }
        assertThat(refusjon3FunnetViaFinnRefusjon.refusjonsgrunnlag.forrigeRefusjonMinusBeløp).isEqualTo(-3966)
        assertThat(refusjon3FunnetViaFinnRefusjon.refusjonsgrunnlag.beregning!!.refusjonsbeløp).isPositive()
        refusjonService.godkjennForArbeidsgiver(refusjon3FunnetViaFinnRefusjon, innloggetArbeidsgiver)

        val minusbeløpRefusjon3 =
            minusbelopRepository.findAllByAvtaleNr(refusjon3.refusjonsgrunnlag.tilskuddsgrunnlag.avtaleNr)
        assertThat(minusbeløpRefusjon3.size).isEqualTo(1)
        assertThat(minusbeløpRefusjon3.first().gjortOpp).isTrue()
        assertThat(minusbeløpRefusjon3.first().gjortOppAvRefusjonId).isEqualTo(refusjon3.id)

    }

    fun opprettRefusjonOgGjørInntektoppslag(tilskuddsperiodeGodkjentMelding: TilskuddsperiodeGodkjentMelding): Refusjon {
        val refusjon = refusjonService.opprettRefusjon(tilskuddsperiodeGodkjentMelding) ?: throw Exception()
        refusjon.status = RefusjonStatus.KLAR_FOR_INNSENDING
        refusjon.unntakOmInntekterFremitid = 0
        refusjon.fristForGodkjenning = Now.localDate().plusDays(1)
        refusjonService.gjørBedriftKontonummeroppslag(refusjon)
        refusjonService.gjørInntektsoppslag(refusjon, innloggetArbeidsgiverBruker)
        // Sett innhentede inntekter til opptjent i periode
        refusjon.refusjonsgrunnlag.inntektsgrunnlag?.inntekter?.filter { it.erMedIInntektsgrunnlag() }?.forEach { it.erOpptjentIPeriode = true }
        // Bekreft at alle inntektene kun er fra tiltaket
        refusjonService.endreBruttolønn(refusjon, true, null)
        refusjonService.gjørBeregning(refusjon, innloggetArbeidsgiverBruker)

        refusjonRepository.save(refusjon)
        return refusjon;
    }

    @Test
    fun `finnRefusjon feiler når strengt fortrolig og ingen adressesperre tilgang`() {
        // Arrange
        val bedriftNr = "999999999"
        val deltakerFnr = "08098613316"
        val melding = TilskuddsperiodeGodkjentMelding(
            avtaleId = "1",
            tilskuddsbeløp = 1000,
            tiltakstype = Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD,
            deltakerEtternavn = "Etternavn",
            deltakerFornavn = "Fornavn",
            arbeidsgiverFornavn = "Test",
            arbeidsgiverEtternavn = "Arbeidsgiver",
            arbeidsgiverTlf = "12345678",
            arbeidsgiveravgiftSats = 0.141,
            avtaleInnholdId = "1",
            bedriftNavn = "Bedrift AS",
            bedriftNr = bedriftNr,
            deltakerFnr = deltakerFnr,
            feriepengerSats = 0.125,
            otpSats = 0.03,
            tilskuddFom = Now.localDate().minusWeeks(4),
            tilskuddTom = Now.localDate().minusDays(1),
            tilskuddsperiodeId = "p1",
            veilederNavIdent = "X123",
            lønnstilskuddsprosent = 50,
            avtaleNr = 1,
            løpenummer = 1,
            resendingsnummer = null,
            enhet = "1000",
            godkjentTidspunkt = LocalDateTime.now()
        )
        val refusjon = refusjonService.opprettRefusjon(melding)!!

        every { altinnTilgangsstyringService.hentTilganger(any(), any(), any()) } returns setOf(
            Organisasjon("","", bedriftNr, "","")
        )
        every { altinnTilgangsstyringService.hentAdressesperreTilganger(any()) } returns emptySet()
        every { persondataService.hentDiskresjonskode(deltakerFnr) } returns Diskresjonskode.STRENGT_FORTROLIG

        val innlogget = InnloggetArbeidsgiver(
            identifikator = "16120102137",
            altinnTilgangsstyringService = altinnTilgangsstyringService,
            refusjonRepository = refusjonRepository,
            korreksjonRepository = korreksjonRepository,
            refusjonService = refusjonService,
            persondataService = persondataService
        )
        assertThrows<TilgangskontrollException> { innlogget.finnRefusjon(refusjon.id) }
    }

    @Test
    fun `finnRefusjon lykkes når strengt fortrolig og har adressesperre tilgang`() {
        val deltakerFnr = "08098613316"
        val periode1start = Now.localDate().minusMonths(4).with(TemporalAdjusters.firstDayOfMonth());
        val periode1slutt = Now.localDate().minusMonths(4).with(TemporalAdjusters.lastDayOfMonth());

        every { altinnTilgangsstyringService.hentAdressesperreTilganger(any()) } returns setOf(
            Organisasjon("","", "999999999", "","")
        )
        every { persondataService.hentDiskresjonskode(deltakerFnr) } returns Diskresjonskode.FORTROLIG

        val tilskuddMelding = TilskuddsperiodeGodkjentMelding(
            avtaleId = "1",
            tilskuddsbeløp = 1000,
            tiltakstype = Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD,
            deltakerEtternavn = "Mus",
            deltakerFornavn = "Mikke",
            arbeidsgiverFornavn = "Arne",
            arbeidsgiverEtternavn = "Arbeidsgiver",
            arbeidsgiverTlf = "41111111",
            arbeidsgiveravgiftSats = 0.141,
            avtaleInnholdId = "1",
            bedriftNavn = "Bedriften AS",
            bedriftNr = "999999999",
            deltakerFnr = deltakerFnr,
            feriepengerSats = 0.125,
            otpSats = 0.03,
            tilskuddFom = periode1start,
            tilskuddTom = periode1slutt,
            tilskuddsperiodeId = "1",
            veilederNavIdent = "X123456",
            lønnstilskuddsprosent = 60,
            avtaleNr = 3456,
            løpenummer = 1,
            resendingsnummer = null,
            enhet = "1000",
            godkjentTidspunkt = LocalDateTime.now()
        )
        // Act (skal ikke kaste)
        val innlogget = InnloggetArbeidsgiver(
            identifikator = "16120102137",
            altinnTilgangsstyringService = altinnTilgangsstyringService,
            refusjonRepository = refusjonRepository,
            korreksjonRepository = korreksjonRepository,
            refusjonService = refusjonService,
            persondataService = persondataService
        )
        innlogget.finnRefusjon(refusjonService.opprettRefusjon(
            tilskuddMelding
        )!!.id)
    }

}
