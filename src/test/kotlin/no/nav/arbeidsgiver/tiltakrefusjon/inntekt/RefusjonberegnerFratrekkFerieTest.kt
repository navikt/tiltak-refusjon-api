package no.nav.arbeidsgiver.tiltakrefusjon.inntekt

import com.github.guepardoapps.kulid.ULID
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.*
import no.nav.arbeidsgiver.tiltakrefusjon.tilskuddsperiode.TilskuddsperiodeGodkjentMelding
import no.nav.arbeidsgiver.tiltakrefusjon.utils.Now
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDate


@SpringBootTest(properties = ["NAIS_APP_IMAGE=test", "tiltak-refusjon.inntektskomponenten.fake=false"])
@ActiveProfiles("local", "wiremock")
@AutoConfigureWireMock(port = 8090)
class RefusjonberegnerFratrekkFerieTest(
    @Autowired
    val refusjonService: RefusjonService,

) {

    val WIREMOCK_IDENT: String = "08098613316"
    val WIREMOCK_VIRKSOMHET_IDENTIFIKATOR: String = "972674818"
    val log: Logger = LoggerFactory.getLogger(javaClass)

    fun lagEnTilskuddsperiodeGodkjentMelding(
        tilskuddFom: LocalDate,
        tilskuddTom: LocalDate,
        tiltakstype: Tiltakstype,
        tilskuddsbeløp: Int,
        deltakerFnr: String,
        bedriftNr: String
    ): TilskuddsperiodeGodkjentMelding {
        val tilskuddsperiodeGodkjentMelding = TilskuddsperiodeGodkjentMelding(
            avtaleId = ULID.random(),
            tilskuddsperiodeId = ULID.random(),
            avtaleInnholdId = ULID.random(),
            deltakerFornavn = "Henrik",
            deltakerEtternavn = "Wergeland",
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
            enhet = "1104"
        )
        return tilskuddsperiodeGodkjentMelding
    }

    fun opprettRefusjonOgGjørInntektoppslag(tilskuddsperiodeGodkjentMelding: TilskuddsperiodeGodkjentMelding): Refusjon {
        val refusjon = refusjonService.opprettRefusjon(tilskuddsperiodeGodkjentMelding) ?: throw Exception()
        refusjon.status = RefusjonStatus.KLAR_FOR_INNSENDING
        refusjon.unntakOmInntekterToMånederFrem = false
        refusjon.fristForGodkjenning = Now.localDate().plusDays(1)
        refusjon.endreBruttolønn(true, null)
        refusjonService.gjørBedriftKontonummeroppslag(refusjon)
        refusjonService.gjørInntektsoppslag(refusjon)
        return refusjon;
    }

    @Test
    fun `hent inntektsoppslag som har feriefratrekk og beregn`() {
        val beregnetBelop: Int = 31283; // 40% av sumUtgifter på 78209
        val trekkgrunnlagFerie: Int = 7500 // trekk grunnlag fra inntektoppslag

        val tilskuddsperiodeGodkjentMelding: TilskuddsperiodeGodkjentMelding = lagEnTilskuddsperiodeGodkjentMelding(
            LocalDate.of(2021, 7, 1),
            LocalDate.of(2021, 7, 30),
            Tiltakstype.SOMMERJOBB,
            60000,
            WIREMOCK_IDENT,
            WIREMOCK_VIRKSOMHET_IDENTIFIKATOR,
        )
        val refusjon = opprettRefusjonOgGjørInntektoppslag(tilskuddsperiodeGodkjentMelding)

        assert(refusjon.refusjonsgrunnlag.beregning!!.refusjonsbeløp == beregnetBelop - trekkgrunnlagFerie)
    }

    @Test
    fun `hent inntektsoppslag som ikke har feriefratrekk og beregn`() {
        val beregnetBelop: Int = 31283; // 40% av sumUtgifter på 78209
        val trekkgrunnlagFerie: Int = 0 // trekk grunnlag fra inntektoppslag

        val tilskuddsperiodeGodkjentMelding: TilskuddsperiodeGodkjentMelding = lagEnTilskuddsperiodeGodkjentMelding(
            LocalDate.of(2021, 6, 1),
            LocalDate.of(2021, 6, 31),
            Tiltakstype.SOMMERJOBB,
            60000,
            WIREMOCK_IDENT,
            WIREMOCK_VIRKSOMHET_IDENTIFIKATOR,
        )
        val refusjon = opprettRefusjonOgGjørInntektoppslag(tilskuddsperiodeGodkjentMelding)

        assert(refusjon.refusjonsgrunnlag.beregning!!.refusjonsbeløp == beregnetBelop - trekkgrunnlagFerie)
    }
}