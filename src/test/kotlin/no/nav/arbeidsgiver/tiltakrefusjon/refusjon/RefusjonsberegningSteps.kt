package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import io.cucumber.datatable.DataTable
import io.cucumber.java8.No
import org.assertj.core.api.Assertions.assertThat
import org.junit.Assume
import java.time.LocalDate
import java.time.YearMonth

class RefusjonsberegningSteps : No {
    init {
        var lonnstilskuddProsent = 0
        var startDato: LocalDate = LocalDate.now()
        var sluttDato: LocalDate = LocalDate.now().plusMonths(1)
        var inntekstlinjer: List<Inntektslinje> = listOf()
        var arbeidsgiveravgift = 0.0
        var feriepengersats = 0.0
        var tjenestepensjon = 0.0

        Gitt("følgende opplysninger om inntekt") { tabell: DataTable ->
            inntekstlinjer = tabell.asMaps().map { map: MutableMap<String, String> ->
                Inntektslinje(
                        map["inntektType"]!!,
                        (map["beløp"])!!.toDouble(),
                        YearMonth.parse(map["måned"]),
                        map["inntektsperiodeFom"]?.let {  LocalDate.parse(it)},
                        map["inntektsperiodeTom"]?.let { LocalDate.parse(it) }
                )
            }
        }
        Og("avtale med arbeidsgiveravgift {string}, feriepengersats {string} og OTP {string}"){ agAvfigt:String, feriesats:String, otp:String ->
            arbeidsgiveravgift  = agAvfigt.toDouble()
            feriepengersats = feriesats.toDouble()
            tjenestepensjon = otp.toDouble()
        }
        Når("lønnstilskudd på {int} prosent skal refunderes for periode {string} til {string}") { angittLonnstilskuddProsent: Int, startDatoString: String, sluttDatoString: String ->
            lonnstilskuddProsent = angittLonnstilskuddProsent
            startDato = LocalDate.parse(startDatoString)
            sluttDato = LocalDate.parse(sluttDatoString)

        }
        Så("beregnes refusjon til {string} kr per måned") { refusjon: String ->
            val beregnet = Refusjonsgrunnlag(inntekstlinjer, lonnstilskuddProsent, startDato, sluttDato,arbeidsgiveravgift, feriepengersats, tjenestepensjon).hentBeregnetGrunnlag()
            assertThat(beregnet).isEqualByComparingTo(refusjon.toInt());
        }
        Before("@skip_scenario") { _ ->
            Assume.assumeTrue("Ignorerer scenario", false)
        }
    }
}