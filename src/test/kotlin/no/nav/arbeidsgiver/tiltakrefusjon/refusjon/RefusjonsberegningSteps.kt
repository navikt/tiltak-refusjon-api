package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import io.cucumber.datatable.DataTable
import io.cucumber.java8.No
import org.assertj.core.api.Assertions.assertThat
import org.junit.Assume
import java.time.LocalDate
import java.time.YearMonth

class RefusjonsberegningSteps : No {
    init {
        lateinit var inntekstlinjer: List<Inntektslinje>
        lateinit var tilskuddsgrunnlag: Tilskuddsgrunnlag

        Gitt("følgende opplysninger om inntekt") { tabell: DataTable ->
            inntekstlinjer = tabell.asMaps().map { map: MutableMap<String, String> ->
                Inntektslinje(
                    map["inntektType"]!!,
                    (map["beløp"])!!.toDouble(),
                    YearMonth.parse(map["måned"]),
                    map["opptjeningsperiodeFom"]?.let { LocalDate.parse(it) },
                    map["opptjeningsperiodeTom"]?.let { LocalDate.parse(it) }
                )
            }
        }
        Når("lønnstilskudd på {int} prosent skal refunderes for periode {string} til {string} med arbeidsgiveravgift {string}, feriepengersats {string}, OTP-sats {string}") { angittLonnstilskuddProsent: Int, startDatoString: String, sluttDatoString: String, agAvfigt: String, feriesats: String, otpSats: String ->
            tilskuddsgrunnlag = Tilskuddsgrunnlag(
                avtaleId = "",
                tilskuddsperiodeId = "",
                deltakerFnr = "",
                deltakerFornavn = "",
                deltakerEtternavn = "",
                veilederNavIdent = "",
                bedriftNr = "",
                bedriftNavn = "",
                tiltakstype = Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD,
                tilskuddFom = LocalDate.parse(startDatoString),
                tilskuddTom = LocalDate.parse(sluttDatoString),
                feriepengerSats = feriesats.toDouble(),
                arbeidsgiveravgiftSats = agAvfigt.toDouble(),
                otpSats = otpSats.toDouble(),
                tilskuddsbeløp = 10000000,
                lønnstilskuddsprosent = angittLonnstilskuddProsent,
                avtaleNr = 3456,
                løpenummer = 3
            )
        }
        Så("beregnes refusjon til {int} kr for periode") { refusjon: Int ->
            val beregnet = beregnRefusjonsbeløp(
                inntekter = inntekstlinjer,
                tilskuddsgrunnlag = tilskuddsgrunnlag
            )
            assertThat(beregnet.refusjonsbeløp).isEqualByComparingTo(refusjon);
        }
        Before("@skip_scenario") { _ ->
            Assume.assumeTrue("Ignorerer scenario", false)
        }
    }
}