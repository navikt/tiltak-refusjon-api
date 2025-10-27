package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import no.nav.arbeidsgiver.tiltakrefusjon.innloggetBruker
import no.nav.arbeidsgiver.tiltakrefusjon.tilskuddsperiode.TilskuddsperiodeGodkjentMelding
import no.nav.arbeidsgiver.tiltakrefusjon.utils.Now
import no.nav.arbeidsgiver.tiltakrefusjon.utils.forrigeÅretsG
import no.nav.arbeidsgiver.tiltakrefusjon.utils.åretsG
import no.nav.arbeidsgiver.tiltakrefusjon.varsling.VarslingRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.TemporalAdjusters.lastDayOfMonth

@SpringBootTest(properties = ["NAIS_APP_IMAGE=test"])
@ActiveProfiles("local")
@AutoConfigureWireMock(port = 8090)
class RefusjonVarig5GTest(
    @Autowired
    val refusjonService: RefusjonService,
    @Autowired
    val refusjonRepository: RefusjonRepository,
    @Autowired
    val varslingRepository: VarslingRepository
) {
    @BeforeEach
    fun setup() {
        varslingRepository.deleteAll()
        refusjonRepository.deleteAll()
    }

    @AfterEach
    fun teardown() {
        varslingRepository.deleteAll()
        refusjonRepository.deleteAll()
        Now.resetClock()
    }

    val innloggetArbeidsgiver = innloggetBruker("12345678901", BrukerRolle.ARBEIDSGIVER);

    @Test
    fun `refusjon som overskrider 5g fører til redusert utbetaling`() {
        listOf(
            tilskuddsmelding,
            tilskuddsmelding.medNyId("februar").medNyPeriode(2024, 2),
            tilskuddsmelding.medNyId("mars").medNyPeriode(2024, 3),
            tilskuddsmelding.medNyId("april").medNyPeriode(2024, 4)
        ).map { it.opprettRefusjonMedJustertTid() }.forEach { godkjennRefusjonMedJustertTid(it) }

        val refusjonFraBaseEtterGodkjenning = refusjonRepository.findAll()
        assertThat(refusjonFraBaseEtterGodkjenning.count()).isEqualTo(4)
        // Avtalen overskrider 5G for 2 av refusjonene
        assertThat(refusjonFraBaseEtterGodkjenning.count { it.over5G() }).isEqualTo(2)
        // Mars-refusjonen vil være redusert i forhold til februar-refusjonen fordi den er over 5G
        assertThat(refusjonFraBaseEtterGodkjenning.tilskuddsperiode("mars").over5G()).isTrue()
        assertThat(refusjonFraBaseEtterGodkjenning.tilskuddsperiode("mars").refusjonsbeløp()).isLessThan(
            refusjonFraBaseEtterGodkjenning.tilskuddsperiode("februar").refusjonsbeløp()
        )
        // April-refusjonen vil være 0 fordi avtalen nådde 5G (maks) i Mars.
        assertThat(refusjonFraBaseEtterGodkjenning.tilskuddsperiode("april").over5G()).isTrue()
        assertThat(refusjonFraBaseEtterGodkjenning.tilskuddsperiode("april").refusjonsbeløp()).isEqualTo(0)
    }

    @Test
    fun `refusjon som ble opprettet først etter nyttår overskrider allikevel 5g i året den gjelder for`() {
        // Denne testen sikrer at logikken for 5G faktisk summerer for riktig år på refusjoner som godkjennes på nyåret.
        listOf(
            tilskuddsmelding,
            tilskuddsmelding.medNyId("februar").medNyPeriode(2024, 2),
            tilskuddsmelding.medNyId("mars").medNyPeriode(2024, 3)
        ).map { it.opprettRefusjonMedJustertTid() }.forEach { godkjennRefusjonMedJustertTid(it) }

        // Når alle tidligere refusjoner er godkjent og behandlet (og har gått over 5G), kommer en ny refusjon for desember,
        // men den blir opprettet i januar året etter.
        val desemberRefusjon = tilskuddsmelding.medNyId("desember").medNyPeriode(2024, 12).opprettRefusjonMedJustertTid()
        godkjennRefusjonMedJustertTid(desemberRefusjon)

        // Desember-refusjonen som ble opprettet etter alle tidligere refusjoner ble godkjent skal også overskride 5G selv om den er opprettet/godkjent på nyåret.
        assertThat(refusjonRepository.findById(desemberRefusjon.id).get().over5G()).isEqualTo(true)
    }

    @Test
    fun `refusjon på avtale som har gått over 5G gir IKKE redusert utbetaling etter årsskifte`() {
        val refusjonsliste = listOf(
            tilskuddsmelding,
            tilskuddsmelding.medNyId("februar").medNyPeriode(2024, 2),
            tilskuddsmelding.medNyId("mars").medNyPeriode(2024, 3),
            tilskuddsmelding.medNyId("januar2025").medNyPeriode(2025, 1)
        ).map { it.opprettRefusjonMedJustertTid() }
        refusjonsliste.forEach { godkjennRefusjonMedJustertTid(it) }

        val refusjonFraBaseEtterGodkjenning = refusjonRepository.findAll()
        assertThat(refusjonFraBaseEtterGodkjenning.count()).isEqualTo(refusjonsliste.size)
        assertThat(refusjonFraBaseEtterGodkjenning.count { it.over5G() }).isEqualTo(1)
        assertThat(refusjonFraBaseEtterGodkjenning.tilskuddsperiode("mars").over5G()).isTrue()
        assertThat(refusjonFraBaseEtterGodkjenning.tilskuddsperiode("januar2025").over5G()).isFalse()
    }

    @Test
    fun `refusjoner for perioden hvor grunnbeløp økes medfører at differansen mellom forrige 5G og ny 5G utbetales`() {
        listOf(
            tilskuddsmelding,
            tilskuddsmelding.medNyId("februar").medNyPeriode(2024, 2),
            tilskuddsmelding.medNyId("mars").medNyPeriode(2024, 3),
            tilskuddsmelding.medNyId("april").medNyPeriode(2024, 4),
            tilskuddsmelding.medNyId("mai").medNyPeriode(2024, 5),
            tilskuddsmelding.medNyId("juni").medNyPeriode(2024, 6)
        ).map { it.opprettRefusjonMedJustertTid() }.forEach { godkjennRefusjonMedJustertTid(it) }

        val alleRefusjoner = refusjonRepository.findAll()
        assertThat(alleRefusjoner.count()).isEqualTo(6)
        assertThat(alleRefusjoner.count { it.over5G() }).isEqualTo(4)
        // April-refusjonen går i 0
        assertThat(alleRefusjoner.tilskuddsperiode("april").over5G()).isTrue()
        assertThat(alleRefusjoner.tilskuddsperiode("april").refusjonsbeløp()).isEqualTo(0)
        // Mai-refusjonen medfører en utbetaling tilsvarende differansen i fem ganger grunnbeløpet etter G har økt
        assertThat(alleRefusjoner.tilskuddsperiode("mai").over5G()).isTrue()
        assertThat(alleRefusjoner.tilskuddsperiode("mai").refusjonsbeløp()).isEqualTo(differanseI5G)
        // Juni-refusjonen går i 0 igjen
        assertThat(alleRefusjoner.tilskuddsperiode("juni").over5G()).isTrue()
        assertThat(alleRefusjoner.tilskuddsperiode("juni").refusjonsbeløp()).isEqualTo(0)
    }

    private fun gjørInntektoppslagForRefusjon(refusjon: Refusjon) {
        // Sett innhentede inntekter til opptjent i periode
        refusjon.refusjonsgrunnlag.inntektsgrunnlag?.inntekter?.filter { it.erMedIInntektsgrunnlag() }?.forEach { it.erOpptjentIPeriode = true }
        // Bekreft at alle inntektene kun er fra tiltaket
        refusjonService.endreBruttolønn(refusjon, true, null)
        refusjonService.gjørBeregning(refusjon, innloggetArbeidsgiver)
        refusjonRepository.save(refusjon)
    }

    private fun godkjennRefusjonMedJustertTid(refusjon: Refusjon) {
        Now.fixedDate(refusjon.fristForGodkjenning.minusDays(10))
        val oppdatertRefusjon = refusjonRepository.findById(refusjon.id).get()
        refusjonService.gjørBedriftKontonummeroppslag(oppdatertRefusjon)
        refusjonService.gjørInntektsoppslag(oppdatertRefusjon, innloggetArbeidsgiver)
        gjørInntektoppslagForRefusjon(oppdatertRefusjon)
        val oppdatertRefusjonIgjen = refusjonRepository.findById(oppdatertRefusjon.id).get()
        refusjonService.godkjennForArbeidsgiver(oppdatertRefusjonIgjen, innloggetArbeidsgiver)
        refusjonRepository.save(oppdatertRefusjonIgjen)
    }

    private fun TilskuddsperiodeGodkjentMelding.opprettRefusjonMedJustertTid(): Refusjon {
        Now.fixedDate(this.tilskuddTom.plusDays(5))
        return refusjonService.opprettRefusjon(this)!!
    }
}

private val deltakerFnr = "08098138758"

private val tilskuddsmelding = TilskuddsperiodeGodkjentMelding(
    avtaleId = "1",
    tilskuddsbeløp = 400000,
    tiltakstype = Tiltakstype.VARIG_LONNSTILSKUDD,
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
    tilskuddFom = LocalDate.of(2024, 1, 1),
    tilskuddTom = LocalDate.of(2024, 1, 31),
    tilskuddsperiodeId = "januar2024",
    veilederNavIdent = "X123456",
    lønnstilskuddsprosent = 75,
    avtaleNr = 3456,
    løpenummer = 1,
    resendingsnummer = null,
    enhet = "1000",
    godkjentTidspunkt = LocalDateTime.of(2024, 2, 1, 0, 0),
    arbeidsgiverKontonummer = "12345678908",
    arbeidsgiverKid = null,
    mentorTimelonn = null,
    mentorAntallTimer = null,
)

private val differanseI5G = (åretsG * 5) - (forrigeÅretsG * 5)

private fun TilskuddsperiodeGodkjentMelding.medNyId(id: String): TilskuddsperiodeGodkjentMelding {
    return this.copy(tilskuddsperiodeId = id)
}

private fun TilskuddsperiodeGodkjentMelding.medNyPeriode(år: Int, måned: Int): TilskuddsperiodeGodkjentMelding {
    return this.copy(
        tilskuddFom = LocalDate.of(år, måned, 1),
        tilskuddTom = LocalDate.of(år, måned, 1).with(lastDayOfMonth()),
        godkjentTidspunkt = LocalDateTime.of(år, måned, 1, 0, 0).plusMonths(1)
    )
}

private fun Refusjon.refusjonsbeløp(): Int {
    return this.refusjonsgrunnlag.beregning?.refusjonsbeløp ?: 0
}

private fun Refusjon.over5G(): Boolean {
    return this.refusjonsgrunnlag.beregning?.overFemGrunnbeløp ?: false
}

private fun List<Refusjon>.tilskuddsperiode(id: String): Refusjon {
    return this.find { it.refusjonsgrunnlag.tilskuddsgrunnlag.tilskuddsperiodeId == id }!!
}
