package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import com.ninjasquad.springmockk.SpykBean
import io.mockk.verify
import no.nav.arbeidsgiver.tiltakrefusjon.FeilkodeException
import no.nav.arbeidsgiver.tiltakrefusjon.inntekt.InntektskomponentService
import no.nav.arbeidsgiver.tiltakrefusjon.tilskuddsperiode.MidlerFrigjortÅrsak
import no.nav.arbeidsgiver.tiltakrefusjon.tilskuddsperiode.TilskuddsperiodeAnnullertMelding
import no.nav.arbeidsgiver.tiltakrefusjon.tilskuddsperiode.TilskuddsperiodeForkortetMelding
import no.nav.arbeidsgiver.tiltakrefusjon.tilskuddsperiode.TilskuddsperiodeGodkjentMelding
import no.nav.arbeidsgiver.tiltakrefusjon.utils.Now
import no.nav.arbeidsgiver.tiltakrefusjon.varsling.VarslingRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth

@SpringBootTest(properties = ["NAIS_APP_IMAGE=test"])
@ActiveProfiles("local")
@AutoConfigureWireMock(port = 8090)
class RefusjonServiceTest(
    @Autowired
    val refusjonService: RefusjonService,
    @Autowired
    val refusjonRepository: RefusjonRepository,
    @Autowired
    val varslingRepository: VarslingRepository
) {
    @SpykBean
    lateinit var inntektskomponentService: InntektskomponentService

    @BeforeEach
    fun setup() {
        varslingRepository.deleteAll()
        refusjonRepository.deleteAll()
    }

    @Test
    fun `godkjennForArbeidsgiver feiler fordi refusjon er ikke klar til innsending`(){

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
            arbeidsgiveravgiftSats = 0.101,
            avtaleInnholdId = "1",
            bedriftNavn = "Bedriften AS",
            bedriftNr = "999999999",
            deltakerFnr = deltakerFnr,
            feriepengerSats = 0.141,
            otpSats = 0.02,
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


        val refusjon1 = refusjonService.opprettRefusjon(tilskuddMelding)!!
        refusjonService.gjørBedriftKontonummeroppslag(refusjon1)
        refusjonService.gjørInntektsoppslag(refusjon1)
        gjørInntektoppslagForRefusjon(refusjon1)

        assertThat(refusjonRepository.findAll().count()).isEqualTo(1)
        assertDoesNotThrow { refusjonService.godkjennForArbeidsgiver(refusjon1,"999999999")}
        assertThrows<FeilkodeException> { refusjonService.godkjennForArbeidsgiver(refusjon1,"999999999")}
    }

    @Test
    fun `rekkefølge GODKJENNING av refusjon, ingen krav til godkjenning i rekkefølge`(){

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
            arbeidsgiveravgiftSats = 0.101,
            avtaleInnholdId = "1",
            bedriftNavn = "Bedriften AS",
            bedriftNr = "999999999",
            deltakerFnr = deltakerFnr,
            feriepengerSats = 0.141,
            otpSats = 0.02,
            tilskuddFom =  Now.localDate().minusMonths(4),
            tilskuddTom = Now.localDate().minusMonths(3).minusDays(1),
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
            arbeidsgiveravgiftSats = 0.101,
            avtaleInnholdId = "2",
            bedriftNavn = "Bedriften AS",
            bedriftNr = "999999999",
            deltakerFnr = deltakerFnr,
            feriepengerSats = 0.141,
            otpSats = 0.02,
            tilskuddFom = Now.localDate().minusMonths(3),
            tilskuddTom = Now.localDate().minusMonths(2).minusDays(1),
            tilskuddsperiodeId = "2",
            veilederNavIdent = "X123456",
            lønnstilskuddsprosent = 60,
            avtaleNr = 3456,
            løpenummer = 2,
            resendingsnummer = null,
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
            arbeidsgiveravgiftSats = 0.101,
            avtaleInnholdId = "2",
            bedriftNavn = "Bedriften AS",
            bedriftNr = "999999999",
            deltakerFnr = deltakerFnr,
            feriepengerSats = 0.141,
            otpSats = 0.02,
            tilskuddFom = Now.localDate().minusMonths(2),
            tilskuddTom = Now.localDate().minusMonths(1).minusDays(1),
            tilskuddsperiodeId = "3",
            veilederNavIdent = "X123456",
            lønnstilskuddsprosent = 60,
            avtaleNr = 3456,
            løpenummer = 3,
            resendingsnummer = null,
            enhet = "1000",
            godkjentTidspunkt = LocalDateTime.now()
        )

        val refusjon1 = refusjonService.opprettRefusjon(tilskuddMelding)!!
        refusjonService.gjørBedriftKontonummeroppslag(refusjon1)
        refusjonService.gjørInntektsoppslag(refusjon1)
        gjørInntektoppslagForRefusjon(refusjon1)

        val refusjon2 = refusjonService.opprettRefusjon(tilskuddMelding2LittEldreMedLøpenummer2)!!
        refusjonService.gjørBedriftKontonummeroppslag(refusjon2)
        refusjonService.gjørInntektsoppslag(refusjon2)
        gjørInntektoppslagForRefusjon(refusjon2)

        val refusjon3 = refusjonService.opprettRefusjon(tilskuddMelding3LittEldreMedLøpenummer3)!!
        refusjonService.gjørBedriftKontonummeroppslag(refusjon3)
        refusjonService.gjørInntektsoppslag(refusjon3)
        gjørInntektoppslagForRefusjon(refusjon3)

        assertThat(refusjonRepository.findAll().count()).isEqualTo(3)
        assertDoesNotThrow { refusjonService.godkjennForArbeidsgiver(refusjon3,"999999999")}
        assertDoesNotThrow { refusjonService.godkjennForArbeidsgiver(refusjon2,"999999999")}
        assertDoesNotThrow { refusjonService.godkjennForArbeidsgiver(refusjon1,"999999999")}
    }

    @Test
    fun `godkjenner refusjon med 0 kr, skal bli en annullering til oebs`() {
        val deltakerFnr = "00000000000"
        // Tilskuddsperiode med nullbeløp. Skal gi godkjent med nullbeløpstatus
        val tilskuddMelding = TilskuddsperiodeGodkjentMelding(
            avtaleId = "1",
            tilskuddsbeløp = 0,
            tiltakstype = Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD,
            deltakerEtternavn = "Mus",
            deltakerFornavn = "Mikke",
            arbeidsgiverFornavn = "Arne",
            arbeidsgiverEtternavn = "Arbeidsgiver",
            arbeidsgiverTlf = "41111111",
            arbeidsgiveravgiftSats = 0.101,
            avtaleInnholdId = "1",
            bedriftNavn = "Bedriften AS",
            bedriftNr = "999999999",
            deltakerFnr = deltakerFnr,
            feriepengerSats = 0.141,
            otpSats = 0.02,
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
        val refusjon = refusjonService.opprettRefusjon(tilskuddMelding)!!
        refusjonService.gjørBedriftKontonummeroppslag(refusjon)
        refusjonService.gjørInntektsoppslag(refusjon)
        gjørInntektoppslagForRefusjon(refusjon)
        assertThat(refusjonRepository.findAll().count()).isEqualTo(1)
        assertDoesNotThrow { refusjonService.godkjennForArbeidsgiver(refusjon,"999999999")}
        val lagretRefusjon = refusjonRepository.findAll().firstOrNull()
        if (lagretRefusjon != null) {
            assertThat(lagretRefusjon.status).isEqualTo(RefusjonStatus.GODKJENT_NULLBELØP)
        }
    }

    @Test
    fun `oppretter, forkorter, og forlenger`() {
        val deltakerFnr = "00000000000"
        val tilskuddMelding = TilskuddsperiodeGodkjentMelding(
            avtaleId = "1",
            tilskuddsbeløp = 1000,
            tiltakstype = Tiltakstype.SOMMERJOBB,
            deltakerEtternavn = "Mus",
            deltakerFornavn = "Mikke",
            arbeidsgiverFornavn = "Arne",
            arbeidsgiverEtternavn = "Arbeidsgiver",
            arbeidsgiverTlf = "41111111",
            arbeidsgiveravgiftSats = 0.101,
             avtaleInnholdId = "2",
            bedriftNavn = "Bedriften AS",
            bedriftNr = "999999999",
            deltakerFnr = deltakerFnr,
            feriepengerSats = 0.141,
            otpSats = 0.02,
            tilskuddFom = Now.localDate().minusWeeks(4).plusDays(1),
            tilskuddTom = Now.localDate(),
            tilskuddsperiodeId = "3",
            veilederNavIdent = "X123456",
            lønnstilskuddsprosent = 60,
            avtaleNr = 3456,
            løpenummer = 3,
            resendingsnummer = null,
            enhet = "1000",
            godkjentTidspunkt = LocalDateTime.now()
        )
        refusjonService.opprettRefusjon(tilskuddMelding)
        val pageable: Pageable = PageRequest.of(0, 100)
        var lagretRefusjon = refusjonRepository.findAllByDeltakerFnrAndStatusInAndRefusjonsgrunnlag_Tilskuddsgrunnlag_TiltakstypeIn(deltakerFnr, RefusjonStatus.values().toList(), Tiltakstype.values().toList(), pageable).content[0]
        assertThat(lagretRefusjon.refusjonsgrunnlag.tilskuddsgrunnlag).isNotNull
        assertThat(lagretRefusjon.status).isEqualTo(RefusjonStatus.FOR_TIDLIG)

        // Forkorting
        val forkortetTilskuddTom = lagretRefusjon.refusjonsgrunnlag.tilskuddsgrunnlag.tilskuddTom.minusDays(1)
        val nyttBeløp = 19
        refusjonService.forkortRefusjon(TilskuddsperiodeForkortetMelding(lagretRefusjon.refusjonsgrunnlag.tilskuddsgrunnlag.tilskuddsperiodeId, nyttBeløp, forkortetTilskuddTom))
        lagretRefusjon = refusjonRepository.findByIdOrNull(lagretRefusjon.id) ?: throw RuntimeException()
        assertThat(lagretRefusjon.refusjonsgrunnlag.tilskuddsgrunnlag.tilskuddTom).isEqualTo(forkortetTilskuddTom)
        assertThat(lagretRefusjon.refusjonsgrunnlag.tilskuddsgrunnlag.tilskuddsbeløp).isEqualTo(nyttBeløp)
        assertThat(lagretRefusjon.status).isEqualTo(RefusjonStatus.KLAR_FOR_INNSENDING)

        // Annullering
        refusjonService.annullerRefusjon(TilskuddsperiodeAnnullertMelding(lagretRefusjon.refusjonsgrunnlag.tilskuddsgrunnlag.tilskuddsperiodeId, MidlerFrigjortÅrsak.AVTALE_ANNULLERT))
        lagretRefusjon = refusjonRepository.findByIdOrNull(lagretRefusjon.id) ?: throw RuntimeException()
        assertThat(lagretRefusjon.status).isEqualTo(RefusjonStatus.ANNULLERT)
    }

    @Test
    fun `kaller opprett på samme melding flere ganger, skal bare lagre den ene`() {
        val deltakerFnr = "00000000000"
        val tilskuddMelding = TilskuddsperiodeGodkjentMelding(
            avtaleId = "1",
            tilskuddsbeløp = 1000,
            tiltakstype = Tiltakstype.SOMMERJOBB,
            deltakerEtternavn = "Mus",
            deltakerFornavn = "Mikke",
            arbeidsgiverFornavn = "Arne",
            arbeidsgiverEtternavn = "Arbeidsgiver",
            arbeidsgiverTlf = "41111111",
            arbeidsgiveravgiftSats = 0.101,
            avtaleInnholdId = "2",
            bedriftNavn = "Bedriften AS",
            bedriftNr = "999999999",
            deltakerFnr = deltakerFnr,
            feriepengerSats = 0.141,
            otpSats = 0.02,
            tilskuddFom = Now.localDate().minusWeeks(4).plusDays(1),
            tilskuddTom = Now.localDate(),
            tilskuddsperiodeId = "3",
            veilederNavIdent = "X123456",
            lønnstilskuddsprosent = 60,
            avtaleNr = 3456,
            løpenummer = 3,
            resendingsnummer = null,
            enhet = "1000",
            godkjentTidspunkt = LocalDateTime.now()
        )
        refusjonService.opprettRefusjon(tilskuddMelding)
        refusjonService.opprettRefusjon(tilskuddMelding)

        assertThat(refusjonRepository.findAll().filter { it.refusjonsgrunnlag.tilskuddsgrunnlag.tilskuddsperiodeId == tilskuddMelding.tilskuddsperiodeId }).hasSize(1)

    }

    @Test
    internal fun `inntektsoppslag skal ta hensyn til unntaksregel`() {
        val deltakerFnr = "00000000000"
        val tilskuddMelding = TilskuddsperiodeGodkjentMelding(
            avtaleId = "2",
            tilskuddsbeløp = 1000,
            tiltakstype = Tiltakstype.SOMMERJOBB,
            deltakerEtternavn = "Mus",
            deltakerFornavn = "Mikke",
            arbeidsgiverFornavn = "Arne",
            arbeidsgiverEtternavn = "Arbeidsgiver",
            arbeidsgiverTlf = "41111111",
            arbeidsgiveravgiftSats = 0.101,
            avtaleInnholdId = "2",
            bedriftNavn = "Bedriften AS",
            bedriftNr = "999999999",
            deltakerFnr = deltakerFnr,
            feriepengerSats = 0.141,
            otpSats = 0.02,
            tilskuddFom = Now.localDate().minusWeeks(4).plusDays(1),
            tilskuddTom = Now.localDate().minusDays(1),
            tilskuddsperiodeId = "4",
            veilederNavIdent = "X123456",
            lønnstilskuddsprosent = 60,
            avtaleNr = 3456,
            løpenummer = 3,
            resendingsnummer = null,
            enhet = "1000",
            godkjentTidspunkt = LocalDateTime.now()
        )
        var refusjon = refusjonService.opprettRefusjon(tilskuddMelding) ?: fail("Skulle kunne opprette refusjon")

        refusjonService.gjørInntektsoppslag(refusjon)
        verify {
            inntektskomponentService.hentInntekter(tilskuddMelding.deltakerFnr, tilskuddMelding.bedriftNr, tilskuddMelding.tilskuddFom, tilskuddMelding.tilskuddTom.plusMonths(1))
        }

        Now.fixedDate(LocalDate.now().plusDays(1))
        refusjon.merkForUnntakOmInntekterToMånederFrem(2)
        refusjonService.gjørInntektsoppslag(refusjon)
        verify {
            inntektskomponentService.hentInntekter(tilskuddMelding.deltakerFnr, tilskuddMelding.bedriftNr, tilskuddMelding.tilskuddFom, tilskuddMelding.tilskuddTom.plusMonths(2))
        }
        Now.resetClock()
    }

    @Test
    internal fun `inntektsoppslag skal ta hensyn til at arbeidsgiver klikker på knapp for å hente neste måneds inntekter`() {
        val deltakerFnr = "00000000000"
        val tilskuddMelding = TilskuddsperiodeGodkjentMelding(
            avtaleId = "2",
            tilskuddsbeløp = 1000,
            tiltakstype = Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD,
            deltakerEtternavn = "Mus",
            deltakerFornavn = "Mikke",
            arbeidsgiverFornavn = "Arne",
            arbeidsgiverEtternavn = "Arbeidsgiver",
            arbeidsgiverTlf = "41111111",
            arbeidsgiveravgiftSats = 0.101,
            avtaleInnholdId = "2",
            bedriftNavn = "Bedriften AS",
            bedriftNr = "999999999",
            deltakerFnr = deltakerFnr,
            feriepengerSats = 0.141,
            otpSats = 0.02,
            tilskuddFom = YearMonth.from(Now.localDate().minusMonths(1)).atDay(1), //Now.localDate().minusWeeks(4).plusDays(1),
            tilskuddTom =YearMonth.from(Now.localDate().minusMonths(1)).atEndOfMonth(), //Now.localDate().minusDays(1),
            tilskuddsperiodeId = "4",
            veilederNavIdent = "X123456",
            lønnstilskuddsprosent = 60,
            avtaleNr = 3456,
            løpenummer = 3,
            resendingsnummer = null,
            enhet = "1000",
            godkjentTidspunkt = LocalDateTime.now()
        )
        var refusjon = refusjonService.opprettRefusjon(tilskuddMelding) ?: fail("Skulle kunne opprette refusjon")

        refusjonService.gjørInntektsoppslag(refusjon)
        verify {
            inntektskomponentService.hentInntekter(tilskuddMelding.deltakerFnr, tilskuddMelding.bedriftNr, tilskuddMelding.tilskuddFom, tilskuddMelding.tilskuddTom.plusMonths(0))
        }

        Now.fixedDate(LocalDate.now().plusDays(1))
        //refusjon.merkForUnntakOmInntekterToMånederFrem(true, "")
        refusjon.merkForHentInntekterFrem(true, "")
        refusjonService.gjørInntektsoppslag(refusjon)
        verify {
            inntektskomponentService.hentInntekter(tilskuddMelding.deltakerFnr, tilskuddMelding.bedriftNr, tilskuddMelding.tilskuddFom, tilskuddMelding.tilskuddTom.plusMonths(1))
        }
        Now.resetClock()
    }

    @Test
    fun `Inntektsoppslag for andre typer enn sommerjobb skal sjekke 1 måned ekstra`() {
        val deltakerFnr = "00000000000"
        val tilskuddMelding = TilskuddsperiodeGodkjentMelding(
            avtaleId = "2",
            tilskuddsbeløp = 1000,
            tiltakstype = Tiltakstype.VARIG_LONNSTILSKUDD,
            deltakerEtternavn = "Mus",
            deltakerFornavn = "Mikke",
            arbeidsgiverFornavn = "Arne",
            arbeidsgiverEtternavn = "Arbeidsgiver",
            arbeidsgiverTlf = "41111111",
            arbeidsgiveravgiftSats = 0.101,
            avtaleInnholdId = "2",
            bedriftNavn = "Bedriften AS",
            bedriftNr = "999999999",
            deltakerFnr = deltakerFnr,
            feriepengerSats = 0.141,
            otpSats = 0.02,
            tilskuddFom = Now.localDate().minusWeeks(4).plusDays(1),
            tilskuddTom = Now.localDate().minusDays(1),
            tilskuddsperiodeId = "4",
            veilederNavIdent = "X123456",
            lønnstilskuddsprosent = 60,
            avtaleNr = 3456,
            løpenummer = 3,
            resendingsnummer = null,
            enhet = "1000",
            godkjentTidspunkt = LocalDateTime.now()
        )
        var refusjon = refusjonService.opprettRefusjon(tilskuddMelding) ?: fail("Skulle kunne opprette refusjon")
        refusjonService.gjørInntektsoppslag(refusjon)
        verify {
            inntektskomponentService.hentInntekter(tilskuddMelding.deltakerFnr, tilskuddMelding.bedriftNr, tilskuddMelding.tilskuddFom, tilskuddMelding.tilskuddTom.plusMonths(0))
        }
        Now.fixedDate(LocalDate.now().plusDays(1))
        refusjon.merkForUnntakOmInntekterToMånederFrem(2)
        refusjonService.gjørInntektsoppslag(refusjon)
        verify {
            inntektskomponentService.hentInntekter(tilskuddMelding.deltakerFnr, tilskuddMelding.bedriftNr, tilskuddMelding.tilskuddFom, tilskuddMelding.tilskuddTom.plusMonths(2))
        }
        Now.resetClock()
    }

    @Test
    fun `Manuell annullering av tilskuddsperiode fordi det ikke vil bli søkt om refusjon annullerer ikke refusjon`() {
        val deltakerFnr = "00000000000"
        val tilskuddMelding = TilskuddsperiodeGodkjentMelding(
            avtaleId = "1",
            tilskuddsbeløp = 1000,
            tiltakstype = Tiltakstype.SOMMERJOBB,
            deltakerEtternavn = "Mus",
            deltakerFornavn = "Mikke",
            arbeidsgiverFornavn = "Arne",
            arbeidsgiverEtternavn = "Arbeidsgiver",
            arbeidsgiverTlf = "41111111",
            arbeidsgiveravgiftSats = 0.101,
            avtaleInnholdId = "2",
            bedriftNavn = "Bedriften AS",
            bedriftNr = "999999999",
            deltakerFnr = deltakerFnr,
            feriepengerSats = 0.141,
            otpSats = 0.02,
            tilskuddFom = Now.localDate().minusWeeks(4).plusDays(1),
            tilskuddTom = Now.localDate(),
            tilskuddsperiodeId = "5",
            veilederNavIdent = "X123456",
            lønnstilskuddsprosent = 60,
            avtaleNr = 3456,
            løpenummer = 3,
            resendingsnummer = null,
            enhet = "1000",
            godkjentTidspunkt = LocalDateTime.now()
        )
        refusjonService.opprettRefusjon(tilskuddMelding)
        assertThat(refusjonRepository.findAll().filter { it.refusjonsgrunnlag.tilskuddsgrunnlag.tilskuddsperiodeId == tilskuddMelding.tilskuddsperiodeId }).hasSize(1)
        var lagretRefusjon = refusjonRepository.findAllByRefusjonsgrunnlag_Tilskuddsgrunnlag_TilskuddsperiodeId(tilskuddMelding.tilskuddsperiodeId).firstOrNull()

        refusjonService.annullerRefusjon(TilskuddsperiodeAnnullertMelding(tilskuddMelding.tilskuddsperiodeId, MidlerFrigjortÅrsak.REFUSJON_IKKE_SØKT))
        lagretRefusjon = refusjonRepository.findByIdOrNull(lagretRefusjon?.id) ?: throw RuntimeException()
        assertThat(lagretRefusjon.status).isNotEqualTo(RefusjonStatus.ANNULLERT)
    }

    fun gjørInntektoppslagForRefusjon(refusjon: Refusjon) {
        // Sett innhentede inntekter til opptjent i periode
        refusjon.refusjonsgrunnlag.inntektsgrunnlag?.inntekter?.filter { it.erMedIInntektsgrunnlag() }?.forEach { it.erOpptjentIPeriode = true }
        // Bekreft at alle inntektene kun er fra tiltaket
        refusjon.endreBruttolønn(true, null)
    }

}