package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import io.cucumber.datatable.DataTable
import io.cucumber.java8.No
import org.assertj.core.api.Assertions.assertThat
import org.junit.Assume
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate
import java.time.YearMonth

class RefusjonsberegningSteps : No {
    init {
        var lonnstilskuddProsent = 0
        var startDato: LocalDate? = null
        var sluttDato: LocalDate? = null
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
                        map["opptjeningsperiodeFom"]?.let {  LocalDate.parse(it)},
                        map["opptjeningsperiodeTom"]?.let { LocalDate.parse(it) }
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
        Så("beregnes refusjon til {string} kr") { refusjon: String ->
            val beregnet = Refusjonsgrunnlag(inntekstlinjer, lonnstilskuddProsent, startDato, sluttDato,arbeidsgiveravgift, feriepengersats, tjenestepensjon).hentBeregnetGrunnlag()
            assertThat(beregnet).isEqualByComparingTo(refusjon.toDouble());
        }
        Så("skal programmet kaste en feil"){
            assertThrows<RefusjonsgrunnlagException> {
                val beregnet = Refusjonsgrunnlag(inntekstlinjer, lonnstilskuddProsent, startDato, sluttDato,arbeidsgiveravgift, feriepengersats, tjenestepensjon).hentBeregnetGrunnlag()
            }
        }
        Before("@skip_scenario") { _ ->
            Assume.assumeTrue("Ignorerer scenario", false)
        }
    }
}