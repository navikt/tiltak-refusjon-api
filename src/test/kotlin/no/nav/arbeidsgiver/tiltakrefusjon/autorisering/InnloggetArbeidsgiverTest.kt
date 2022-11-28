package no.nav.arbeidsgiver.tiltakrefusjon.autorisering

import com.ninjasquad.springmockk.MockkBean
import com.ninjasquad.springmockk.SpykBean
import io.mockk.every
import no.nav.arbeidsgiver.tiltakrefusjon.altinn.AltinnTilgangsstyringService
import no.nav.arbeidsgiver.tiltakrefusjon.altinn.Organisasjon
import no.nav.arbeidsgiver.tiltakrefusjon.inntekt.InntektskomponentService
import no.nav.arbeidsgiver.tiltakrefusjon.organisasjon.EregClient
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.KorreksjonRepository
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.RefusjonRepository
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.RefusjonService
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.Tiltakstype
import no.nav.arbeidsgiver.tiltakrefusjon.tilskuddsperiode.TilskuddsperiodeGodkjentMelding
import no.nav.arbeidsgiver.tiltakrefusjon.utils.Now
import no.nav.arbeidsgiver.tiltakrefusjon.varsling.VarslingRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.BeforeEach
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
            enhet = "1000",
            godkjentTidspunkt = LocalDateTime.now()
        )
        val tilskuddMelding2LittEldreMedLøpenummer2 = TilskuddsperiodeGodkjentMelding(
            avtaleId = "1",
            tilskuddsbeløp = 1000,
            tiltakstype = Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD,
            deltakerEtternavn = "Mus",
            deltakerFornavn = "Mikke",
            arbeidsgiveravgiftSats = 0.101,
            avtaleInnholdId = "2",
            bedriftNavn = "Bedriften AS",
            bedriftNr = "999999999",
            deltakerFnr = deltakerFnr,
            feriepengerSats = 0.141,
            otpSats = 0.02,
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
    }

    //TODO : TEST DER DET ER INGEN RESULTAT FOR GODKJENT MINUS bELØP I REPO
    //TODO Hvis det er flere tidligere minus beløp = sorter på løpenummer og finn den SISTE somer den NYESTE løpenummer!
    //DONE: Test som henter to refusjoner, hvor den første har minus beløp som kommer i den nye som skal sendes inn.
    @Test
    fun finnRefusjonMedMinusBeløpFraForrigeRefusjon(){
        val deltakerFnr = "08098613316"
        val tilskuddMelding = TilskuddsperiodeGodkjentMelding(
            avtaleId = "1",
            tilskuddsbeløp = 1000,
            tiltakstype = Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD,
            deltakerEtternavn = "Mus",
            deltakerFornavn = "Mikke",
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
            enhet = "1000",
            godkjentTidspunkt = LocalDateTime.now()
        )
        val tilskuddMelding2LittEldreMedLøpenummer2 = TilskuddsperiodeGodkjentMelding(
            avtaleId = "1",
            tilskuddsbeløp = 1000,
            tiltakstype = Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD,
            deltakerEtternavn = "Mus",
            deltakerFornavn = "Mikke",
            arbeidsgiveravgiftSats = 0.101,
            avtaleInnholdId = "2",
            bedriftNavn = "Bedriften AS",
            bedriftNr = "999999999",
            deltakerFnr = deltakerFnr,
            feriepengerSats = 0.141,
            otpSats = 0.02,
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
            arbeidsgiveravgiftSats = 0.101,
            avtaleInnholdId = "2",
            bedriftNavn = "Bedriften AS",
            bedriftNr = "999999999",
            deltakerFnr = deltakerFnr,
            feriepengerSats = 0.141,
            otpSats = 0.02,
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

        val refusjon1 = refusjonService.opprettRefusjon(tilskuddMelding)!!
        refusjonService.gjørBedriftKontonummeroppslag(refusjon1)
        refusjon1.endreBruttolønn(true,null)
        refusjonService.gjørInntektsoppslag(refusjon1)
        refusjonService.godkjennForArbeidsgiver(refusjon1,"999999999")

        val refusjon2 = refusjonService.opprettRefusjon(tilskuddMelding2LittEldreMedLøpenummer2)!!
        refusjonService.gjørBedriftKontonummeroppslag(refusjon2)
        refusjon2.endreBruttolønn(true,null)
        refusjonService.gjørInntektsoppslag(refusjon2)
        refusjonService.godkjennForArbeidsgiver(refusjon2,"999999999")

        val refusjon3 = refusjonService.opprettRefusjon(tilskuddMelding3LittEldreMedLøpenummer3)!!
        refusjonService.gjørBedriftKontonummeroppslag(refusjon3)
        refusjon3.endreBruttolønn(true,null)
        refusjonService.gjørInntektsoppslag(refusjon3)



        every { altinnTilgangsstyringService.hentTilganger(any()) } returns setOf<Organisasjon>(Organisasjon("Bedrift AS", "Bedrift type", "999999999","Org form","Status"))
        val innloggetArbeidsgiver = InnloggetArbeidsgiver("12345678901",altinnTilgangsstyringService,refusjonRepository,korreksjonRepository,refusjonService,eregClient)

        val refusjon2FunnetViaFinnRefusjon = innloggetArbeidsgiver.finnRefusjon(refusjon2.id)
        val refusjon1FunnetViaFinnRefusjon = innloggetArbeidsgiver.finnRefusjon(refusjon1.id)
        val refusjon3FunnetViaFinnRefusjon = innloggetArbeidsgiver.finnRefusjon(refusjon3.id)

        assertThat(refusjon1FunnetViaFinnRefusjon.refusjonsgrunnlag.forrigeRefusjonMinusBeløp).isEqualTo(0)
        assertThat(refusjon1FunnetViaFinnRefusjon.refusjonsgrunnlag.beregning!!.refusjonsbeløp).isLessThan(0)

        assertThat(refusjon2FunnetViaFinnRefusjon).isEqualTo(refusjon2)
        assertThat(refusjon2FunnetViaFinnRefusjon.refusjonsgrunnlag.forrigeRefusjonMinusBeløp).isLessThan(0)
        assertThat(refusjon2FunnetViaFinnRefusjon.refusjonsgrunnlag.forrigeRefusjonMinusBeløp).isEqualTo(refusjon1.beregning!!.refusjonsbeløp)
        assertThat(refusjon2FunnetViaFinnRefusjon.beregning!!.refusjonsbeløp).isEqualTo(refusjon1.beregning?.refusjonsbeløp!! + refusjon2.beregning!!.refusjonsbeløp)

    }

}
