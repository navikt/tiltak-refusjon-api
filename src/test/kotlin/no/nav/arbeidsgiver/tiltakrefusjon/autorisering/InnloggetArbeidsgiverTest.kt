package no.nav.arbeidsgiver.tiltakrefusjon.autorisering

import com.ninjasquad.springmockk.MockkBean
import com.ninjasquad.springmockk.SpykBean
import io.mockk.every
import no.nav.arbeidsgiver.tiltakrefusjon.altinn.AltinnTilgangsstyringService
import no.nav.arbeidsgiver.tiltakrefusjon.altinn.Organisasjon
import no.nav.arbeidsgiver.tiltakrefusjon.inntekt.InntektskomponentService
import no.nav.arbeidsgiver.tiltakrefusjon.organisasjon.EregClient
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
) {
    @SpykBean
    lateinit var inntektskomponentService: InntektskomponentService
    @MockkBean lateinit var altinnTilgangsstyringService: AltinnTilgangsstyringService
    @MockkBean lateinit var korreksjonRepository: KorreksjonRepository
    @MockkBean lateinit var eregClient: EregClient
    @BeforeEach
    fun setup() {
        varslingRepository.deleteAll()
        refusjonRepository.deleteAll()
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
            enhet = "1000",
            godkjentTidspunkt = LocalDateTime.now()
        )

        val refusjon1 = refusjonService.opprettRefusjon(tilskuddMelding)!!
        val refusjon2 = refusjonService.opprettRefusjon(tilskuddMelding2LittEldreMedLøpenummer2)!!


        every { altinnTilgangsstyringService.hentTilganger(any()) } returns setOf<Organisasjon>(Organisasjon("Bedrift AS", "Bedrift type", "999999999","Org form","Status"))
        val innloggetArbeidsgiver = InnloggetArbeidsgiver("12345678901",altinnTilgangsstyringService,refusjonRepository,korreksjonRepository,refusjonService,eregClient)
        val refusjonFunnet = innloggetArbeidsgiver.finnRefusjon(refusjon1.id)
        assertThat(refusjonFunnet).isEqualTo(refusjon1)

        val refusjonFunnet2 = innloggetArbeidsgiver.finnRefusjon(refusjon2.id)
        assertThat(refusjonFunnet2).isEqualTo(refusjon2)
    }

    @Test
    fun finnRefusjonMedMinusBeløpFraForrigeRefusjon(){
        val deltakerFnr = "08098613316"
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
            enhet = "1000",
            godkjentTidspunkt = LocalDateTime.now()
        )

        val tilskuddMelding3LittEldreMedLøpenummer3 = TilskuddsperiodeGodkjentMelding(
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
            tilskuddsperiodeId = "3",
            veilederNavIdent = "X123456",
            lønnstilskuddsprosent = 60,
            avtaleNr = 3456,
            løpenummer = 3,
            enhet = "1000",
            godkjentTidspunkt = LocalDateTime.now()
        )

        val refusjon1 = opprettRefusjonOgGjørInntektoppslag(tilskuddMelding)
        refusjonService.godkjennForArbeidsgiver(refusjon1,"999999999")

        val refusjon2 = opprettRefusjonOgGjørInntektoppslag(tilskuddMelding2LittEldreMedLøpenummer2)
        refusjonService.godkjennForArbeidsgiver(refusjon2,"999999999")

        val refusjon3 = opprettRefusjonOgGjørInntektoppslag(tilskuddMelding3LittEldreMedLøpenummer3)

        every { altinnTilgangsstyringService.hentTilganger(any()) } returns setOf<Organisasjon>(Organisasjon("Bedrift AS", "Bedrift type", "999999999","Org form","Status"))
        val innloggetArbeidsgiver = InnloggetArbeidsgiver("12345678901",altinnTilgangsstyringService,refusjonRepository,korreksjonRepository,refusjonService,eregClient)

        val refusjon2FunnetViaFinnRefusjon = innloggetArbeidsgiver.finnRefusjon(refusjon2.id)
        val refusjon1FunnetViaFinnRefusjon = innloggetArbeidsgiver.finnRefusjon(refusjon1.id)
        val refusjon3FunnetViaFinnRefusjon = innloggetArbeidsgiver.finnRefusjon(refusjon3.id)

        assertThat(refusjon1FunnetViaFinnRefusjon.refusjonsgrunnlag.forrigeRefusjonMinusBeløp).isEqualTo(0)
        assertThat(refusjon1FunnetViaFinnRefusjon.refusjonsgrunnlag.beregning!!.refusjonsbeløp).isEqualTo(-3966)

        assertThat(refusjon2FunnetViaFinnRefusjon).isEqualTo(refusjon2)
        assertThat(refusjon2FunnetViaFinnRefusjon.refusjonsgrunnlag.forrigeRefusjonMinusBeløp).isLessThan(0)
        assertThat(refusjon2FunnetViaFinnRefusjon.refusjonsgrunnlag.forrigeRefusjonMinusBeløp).isEqualTo(-3966)
        assertThat(refusjon2FunnetViaFinnRefusjon.forrigeRefusjonSomSkalSendesFørst).isEqualTo(null)


        assertThat(refusjon3FunnetViaFinnRefusjon).isEqualTo(refusjon3)
        assertThat(refusjon3FunnetViaFinnRefusjon.refusjonsgrunnlag.forrigeRefusjonMinusBeløp).isLessThan(0)
        assertThat(refusjon3FunnetViaFinnRefusjon.refusjonsgrunnlag.forrigeRefusjonMinusBeløp).isEqualTo(-3966)
        assertThat(refusjon3FunnetViaFinnRefusjon.beregning).isNull()
        assertThat(refusjon3FunnetViaFinnRefusjon.forrigeRefusjonSomSkalSendesFørst).isEqualTo(null)
    }

    @Test
    fun testSettOmForrigeRefusjonMåSendesFørst(){
        val deltakerFnr = "08098613316"
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
            enhet = "1000",
            godkjentTidspunkt = LocalDateTime.now()
        )

        val tilskuddMelding3LittEldreMedLøpenummer3 = TilskuddsperiodeGodkjentMelding(
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
            tilskuddsperiodeId = "3",
            veilederNavIdent = "X123456",
            lønnstilskuddsprosent = 60,
            avtaleNr = 3456,
            løpenummer = 3,
            enhet = "1000",
            godkjentTidspunkt = LocalDateTime.now()
        )

        val refusjon1 = opprettRefusjonOgGjørInntektoppslag(tilskuddMelding)
        refusjonService.godkjennForArbeidsgiver(refusjon1,"999999999")

        val refusjon2 = opprettRefusjonOgGjørInntektoppslag(tilskuddMelding2LittEldreMedLøpenummer2)

        val refusjon3 = opprettRefusjonOgGjørInntektoppslag(tilskuddMelding3LittEldreMedLøpenummer3)

        every { altinnTilgangsstyringService.hentTilganger(any()) } returns setOf<Organisasjon>(Organisasjon("Bedrift AS", "Bedrift type", "999999999","Org form","Status"))
        val innloggetArbeidsgiver = InnloggetArbeidsgiver("12345678901",altinnTilgangsstyringService,refusjonRepository,korreksjonRepository,refusjonService,eregClient)

        val refusjon2FunnetViaFinnRefusjon = innloggetArbeidsgiver.finnRefusjon(refusjon2.id)
        val refusjon1FunnetViaFinnRefusjon = innloggetArbeidsgiver.finnRefusjon(refusjon1.id)
        val refusjon3FunnetViaFinnRefusjon = innloggetArbeidsgiver.finnRefusjon(refusjon3.id)

        assertThat(refusjon1FunnetViaFinnRefusjon.refusjonsgrunnlag.forrigeRefusjonMinusBeløp).isEqualTo(0)
        assertThat(refusjon1FunnetViaFinnRefusjon.refusjonsgrunnlag.beregning!!.refusjonsbeløp).isEqualTo(-3966)

        assertThat(refusjon2FunnetViaFinnRefusjon.refusjonsgrunnlag.forrigeRefusjonMinusBeløp).isLessThan(0)
        assertThat(refusjon2FunnetViaFinnRefusjon.refusjonsgrunnlag.forrigeRefusjonMinusBeløp).isEqualTo(-3966)
        assertThat(refusjon2FunnetViaFinnRefusjon.forrigeRefusjonSomSkalSendesFørst).isEqualTo(null)


        assertThat(refusjon3FunnetViaFinnRefusjon.forrigeRefusjonSomSkalSendesFørst).isEqualTo(refusjon2)

        refusjonService.godkjennForArbeidsgiver(refusjon2,"999999999")
        val refusjon3FunnetViaFinnRefusjonIgjen = innloggetArbeidsgiver.finnRefusjon(refusjon3.id)
        // PEKER IKKE PÅ SEG SELV i settOmForrigeRefusjonMåSendesFørst Og er derfor null
        assertThat(refusjon3FunnetViaFinnRefusjonIgjen.forrigeRefusjonSomSkalSendesFørst).isEqualTo(null)

    }

    @Test
    fun finnRefusjonMedMinusBeløpFraForrigeRefusjonSidenDetErUlikAvtaleNrSkalMinusBeløpetIkkeTasMedIAndreRefusjoner(){
        val deltakerFnr = "08098613316"
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
            avtaleNr = 1234,
            løpenummer = 1,
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
            avtaleNr = 12038,
            løpenummer = 2,
            enhet = "1000",
            godkjentTidspunkt = LocalDateTime.now()
        )

        val tilskuddMelding3LittEldreMedLøpenummer3 = TilskuddsperiodeGodkjentMelding(
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
            tilskuddsperiodeId = "3",
            veilederNavIdent = "X123456",
            lønnstilskuddsprosent = 60,
            avtaleNr = 310178,
            løpenummer = 3,
            enhet = "1000",
            godkjentTidspunkt = LocalDateTime.now()
        )

        val refusjon1 = opprettRefusjonOgGjørInntektoppslag(tilskuddMelding)!!
        refusjonService.godkjennForArbeidsgiver(refusjon1,"999999999")

        val refusjon2 = opprettRefusjonOgGjørInntektoppslag(tilskuddMelding2LittEldreMedLøpenummer2)!!
        refusjonService.godkjennForArbeidsgiver(refusjon2,"999999999")

        val refusjon3 = opprettRefusjonOgGjørInntektoppslag(tilskuddMelding3LittEldreMedLøpenummer3)!!
        refusjonService.gjørInntektsoppslag(refusjon3)

        every { altinnTilgangsstyringService.hentTilganger(any()) } returns setOf<Organisasjon>(Organisasjon("Bedrift AS", "Bedrift type", "999999999","Org form","Status"))
        val innloggetArbeidsgiver = InnloggetArbeidsgiver("12345678901",altinnTilgangsstyringService,refusjonRepository,korreksjonRepository,refusjonService,eregClient)

        val refusjon2FunnetViaFinnRefusjon = innloggetArbeidsgiver.finnRefusjon(refusjon2.id)
        val refusjon1FunnetViaFinnRefusjon = innloggetArbeidsgiver.finnRefusjon(refusjon1.id)
        val refusjon3FunnetViaFinnRefusjon = innloggetArbeidsgiver.finnRefusjon(refusjon3.id)

        assertThat(refusjon1FunnetViaFinnRefusjon.refusjonsgrunnlag.forrigeRefusjonMinusBeløp).isEqualTo(0)
        assertThat(refusjon1FunnetViaFinnRefusjon.refusjonsgrunnlag.beregning!!.lønnFratrukketFerie).isEqualTo(-5000)
        assertThat(refusjon1FunnetViaFinnRefusjon.refusjonsgrunnlag.beregning!!.refusjonsbeløp).isEqualTo(-3966)

        assertThat(refusjon2FunnetViaFinnRefusjon).isEqualTo(refusjon2)
        assertThat(refusjon2FunnetViaFinnRefusjon.refusjonsgrunnlag.forrigeRefusjonMinusBeløp).isEqualTo(0)

        assertThat(refusjon3FunnetViaFinnRefusjon).isEqualTo(refusjon3)
        assertThat(refusjon3FunnetViaFinnRefusjon.refusjonsgrunnlag.forrigeRefusjonMinusBeløp).isEqualTo(0)
    }
    @Test
    fun finnRefusjonMedMinusBeløpFraIKKEForrigeRefusjonSkalIgnorereDetSidenDetIkkeErRettEtterDetDuSkalSendeInn(){
        val deltakerFnr = "08098613316"
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
            enhet = "1000",
            godkjentTidspunkt = LocalDateTime.now()
        )
        val tilskuddMelding2LittEldreMedLøpenummer2MedMinusBeløp = TilskuddsperiodeGodkjentMelding(
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
            enhet = "1000",
            godkjentTidspunkt = LocalDateTime.now()
        )

        val tilskuddMelding3LittEldreMedLøpenummer3UtenFerieTrekk = TilskuddsperiodeGodkjentMelding(
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
            deltakerFnr = "01092211111",
            feriepengerSats = 0.125,
            otpSats = 0.03,
            tilskuddFom = Now.localDate().minusWeeks(3),
            tilskuddTom = Now.localDate().minusDays(1),
            tilskuddsperiodeId = "3",
            veilederNavIdent = "X123456",
            lønnstilskuddsprosent = 60,
            avtaleNr = 3456,
            løpenummer = 3,
            enhet = "1000",
            godkjentTidspunkt = LocalDateTime.now()
        )

        val refusjon1 = opprettRefusjonOgGjørInntektoppslag(tilskuddMeldingUtenFerieTrekk)
        refusjonService.godkjennForArbeidsgiver(refusjon1,"999999999")

        val refusjon2 = opprettRefusjonOgGjørInntektoppslag(tilskuddMelding2LittEldreMedLøpenummer2MedMinusBeløp)
        refusjonService.godkjennForArbeidsgiver(refusjon2,"999999999")

        val refusjon3 = opprettRefusjonOgGjørInntektoppslag(tilskuddMelding3LittEldreMedLøpenummer3UtenFerieTrekk)
        refusjonService.godkjennForArbeidsgiver(refusjon3,"999999999")

        every { altinnTilgangsstyringService.hentTilganger(any()) } returns setOf<Organisasjon>(Organisasjon("Bedrift AS", "Bedrift type", "999999999","Org form","Status"))
        val innloggetArbeidsgiver = InnloggetArbeidsgiver("12345678901",altinnTilgangsstyringService,refusjonRepository,korreksjonRepository,refusjonService,eregClient)

        val refusjon1FunnetViaFinnRefusjon = innloggetArbeidsgiver.finnRefusjon(refusjon1.id)
        val refusjon2FunnetViaFinnRefusjon = innloggetArbeidsgiver.finnRefusjon(refusjon2.id)
        val refusjon3FunnetViaFinnRefusjon = innloggetArbeidsgiver.finnRefusjon(refusjon3.id)

        assertThat(refusjon1FunnetViaFinnRefusjon.refusjonsgrunnlag.forrigeRefusjonMinusBeløp).isEqualTo(0)
        assertThat(refusjon1FunnetViaFinnRefusjon.refusjonsgrunnlag.beregning!!.refusjonsbeløp).isGreaterThan(0)

        assertThat(refusjon2FunnetViaFinnRefusjon).isEqualTo(refusjon2)
        assertThat(refusjon2FunnetViaFinnRefusjon.refusjonsgrunnlag.forrigeRefusjonMinusBeløp).isEqualTo(0)
        assertThat(refusjon2FunnetViaFinnRefusjon.refusjonsgrunnlag.beregning!!.refusjonsbeløp).isEqualTo(-3966)
        assertThat(refusjon2FunnetViaFinnRefusjon.refusjonsgrunnlag.beregning!!.lønnFratrukketFerie).isEqualTo(-5000)

        assertThat(refusjon3FunnetViaFinnRefusjon.refusjonsgrunnlag.beregning!!.refusjonsbeløp).isEqualTo(-2966)
        assertThat(refusjon3FunnetViaFinnRefusjon.refusjonsgrunnlag.forrigeRefusjonMinusBeløp).isEqualTo(-3966)
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
            enhet = "1000",
            godkjentTidspunkt = LocalDateTime.now()
        )


        val refusjon1 = opprettRefusjonOgGjørInntektoppslag(tilskuddMeldingUtenFerieTrekk)!!
        refusjonService.godkjennForArbeidsgiver(refusjon1,"999999999")


        every { altinnTilgangsstyringService.hentTilganger(any()) } returns setOf<Organisasjon>(Organisasjon("Bedrift AS", "Bedrift type", "999999999","Org form","Status"))
        val innloggetArbeidsgiver = InnloggetArbeidsgiver("12345678901",altinnTilgangsstyringService,refusjonRepository,korreksjonRepository,refusjonService,eregClient)

        val refusjon1FunnetViaFinnRefusjon = innloggetArbeidsgiver.finnRefusjon(refusjon1.id)

        assertThat(refusjon1FunnetViaFinnRefusjon.refusjonsgrunnlag.forrigeRefusjonMinusBeløp).isEqualTo(0)
        assertThat(refusjon1FunnetViaFinnRefusjon.refusjonsgrunnlag.beregning!!.refusjonsbeløp).isGreaterThan(0)
    }

    fun opprettRefusjonOgGjørInntektoppslag(tilskuddsperiodeGodkjentMelding: TilskuddsperiodeGodkjentMelding): Refusjon {
        val refusjon = refusjonService.opprettRefusjon(tilskuddsperiodeGodkjentMelding) ?: throw Exception()
        refusjon.status = RefusjonStatus.KLAR_FOR_INNSENDING
        refusjon.unntakOmInntekterToMånederFrem = false
        refusjon.fristForGodkjenning = Now.localDate().plusDays(1)
        refusjonService.gjørBedriftKontonummeroppslag(refusjon)
        refusjonService.gjørInntektsoppslag(refusjon)
        // Sett innhentede inntekter til opptjent i periode
        refusjon.inntektsgrunnlag?.inntekter?.filter { it.erMedIInntektsgrunnlag() }?.forEach { it.erOpptjentIPeriode = true }
        // Bekreft at alle inntektene kun er fra tiltaket
        refusjon.endreBruttolønn(true, null)
        return refusjon;
    }

}
