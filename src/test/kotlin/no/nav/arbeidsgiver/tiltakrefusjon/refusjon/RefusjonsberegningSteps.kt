package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import io.cucumber.datatable.DataTable
import io.cucumber.java.Before
import io.cucumber.java.no.Gitt
import io.cucumber.java.no.Når
import io.cucumber.java.no.Og
import io.cucumber.java.no.Så
import io.cucumber.spring.CucumberContextConfiguration
import org.assertj.core.api.Assertions.assertThat
import org.junit.Assume
import org.springframework.test.context.ContextConfiguration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth

@CucumberContextConfiguration
@ContextConfiguration
class RefusjonsberegningSteps {

    lateinit var inntekstlinjer: List<Inntektslinje>
    lateinit var tilskuddsgrunnlag: Tilskuddsgrunnlag
    var tidligereUtbetalt: Int = 0
    var korreksjonsgrunner = mutableSetOf<Korreksjonsgrunn>()
    var korrigertBruttoLønn: Int? = null

    @Gitt("følgende opplysninger om inntekt")
    fun gitt(tabell: DataTable) {
        inntekstlinjer = tabell.asMaps().map { map: MutableMap<String, String> ->
            Inntektslinje(
                map["inntektType"]!!,
                map["beskrivelse"]!!,
                (map["beløp"])!!.toDouble(),
                YearMonth.parse(map["måned"]),
                map["opptjeningsperiodeFom"]?.let { LocalDate.parse(it) },
                map["opptjeningsperiodeTom"]?.let { LocalDate.parse(it) },
                map["erOpptjentIPeriode"].toBoolean(),
            )
        }
    }

    @Når("sommerjobb på {int} prosent skal refunderes for periode {string} til {string} med arbeidsgiveravgift {string}, feriepengersats {string}, OTP-sats {string}")
    fun når(
        angittLonnstilskuddProsent: Int,
        startDatoString: String,
        sluttDatoString: String,
        agAvfigt: String,
        feriesats: String,
        otpSats: String,
    ) {
        tilskuddsgrunnlag = Tilskuddsgrunnlag(
            avtaleId = "",
            tilskuddsperiodeId = "",
            deltakerFnr = "",
            deltakerFornavn = "",
            deltakerEtternavn = "",
            arbeidsgiverFornavn = "",
            arbeidsgiverEtternavn = "",
            arbeidsgiverTlf = "",
            veilederNavIdent = "",
            bedriftNr = "",
            bedriftNavn = "",
            tiltakstype = Tiltakstype.SOMMERJOBB,
            tilskuddFom = LocalDate.parse(startDatoString),
            tilskuddTom = LocalDate.parse(sluttDatoString),
            feriepengerSats = feriesats.toDouble(),
            arbeidsgiveravgiftSats = agAvfigt.toDouble(),
            otpSats = otpSats.toDouble(),
            tilskuddsbeløp = 10000000,
            lønnstilskuddsprosent = angittLonnstilskuddProsent,
            avtaleNr = 3456,
            løpenummer = 3,
            resendingsnummer = null,
            enhet = "1000",
            godkjentAvBeslutterTidspunkt = LocalDateTime.now()
        )
    }

    @Og("tilskuddsbeløp er {int} kr")
    fun ogTilskuddsbeløp(tilskuddsbeløp: Int) {
        tilskuddsgrunnlag.tilskuddsbeløp = tilskuddsbeløp
    }

    @Og("tidligere utbetalt er {int} kr")
    fun ogTidligereUtbetalt(refusjonsbeløp: Int) {
        tidligereUtbetalt = refusjonsbeløp
    }

    @Og("korreksjonsgrunn {string} er valgt")
    fun ogKorreksjonsgrunn(korreksjonsgrunn: String) {
        korreksjonsgrunner.add(Korreksjonsgrunn.valueOf(korreksjonsgrunn))
    }

    @Og("bruttolønn er korrigert til {int} kr")
    fun ogKorrigertBruttoLønn(lønn: Int) {
        korrigertBruttoLønn = lønn
    }

    @Så("beregnes refusjon til {int} kr for periode")
    fun så(refusjon: Int) {
        val beregnet = beregnRefusjonsbeløp(
            inntekter = inntekstlinjer,
            tilskuddsgrunnlag = tilskuddsgrunnlag,
            tidligereUtbetalt,
            korrigertBruttoLønn,
            tilskuddFom = tilskuddsgrunnlag.tilskuddFom,
            harFerietrekkForSammeMåned = false
        )
        assertThat(beregnet.refusjonsbeløp).isEqualByComparingTo(refusjon);
    }

    @Before("@skip_scenario")
    fun before() {
        Assume.assumeTrue("Ignorerer scenario", false)
    }
}