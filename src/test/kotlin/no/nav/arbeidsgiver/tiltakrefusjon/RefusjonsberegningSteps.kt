package no.nav.arbeidsgiver.tiltakrefusjon

import io.cucumber.datatable.DataTable
import io.cucumber.java8.No
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.Refusjonsgrunnlag
import org.assertj.core.api.Assertions.assertThat
import org.junit.Assume
import java.math.BigDecimal
import java.time.LocalDate
import java.time.YearMonth

class RefusjonsberegningSteps : No {
    init {
        var prosent = 0
        var startDato: LocalDate? = null
        var sluttDato: LocalDate? = null
        var inntekstlinjer: List<Inntektslinje> = listOf()
        Gitt("følgende opplysninger om inntekt") { tabell: DataTable ->
            inntekstlinjer = tabell.asMaps().map { map: MutableMap<String, String> ->
                Inntektslinje(
                        map["inntektType"]!!,
                        BigDecimal(map["beløp"]),
                        YearMonth.parse(map["måned"]),
                        map["opptjeningsperiodeFom"]?.let { LocalDate.parse(it) },
                        map["opptjeningsperiodeTom"]?.let { LocalDate.parse(it) }
                )
            }
        }
        Når("lønnstilskudd på {int} prosent skal refunderes for periode {string} til {string}") { lønnstilskuddProsent: Int, startDatoString: String, sluttDatoString: String ->
            prosent = lønnstilskuddProsent
            startDato = LocalDate.parse(startDatoString)
            sluttDato = LocalDate.parse(sluttDatoString)

        }
        Så("beregnes refusjon til {string} kr") { refusjon: String ->
            val beregnet = beregnRefusjon(Refusjonsgrunnlag(inntekstlinjer, prosent, startDato, sluttDato))
            assertThat(beregnet).isEqualByComparingTo(refusjon);
        }
        Before("@skip_scenario") { _ ->
            Assume.assumeTrue("Ignorerer scenario", false)
        }
    }
}