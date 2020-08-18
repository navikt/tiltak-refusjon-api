package no.nav.arbeidsgiver.tiltakrefusjon

import io.cucumber.core.backend.Pending
import io.cucumber.datatable.DataTable
import io.cucumber.java8.No
import io.cucumber.java8.PendingException
import org.assertj.core.api.Assertions.assertThat
import org.junit.Assume
import java.math.BigDecimal
import java.time.LocalDate

class RefusjonsberegningSteps : No {
    init {
        var prosent = 0
        var startDato: LocalDate? = null
        var sluttDato: LocalDate? = null
        var inntekstlinjer: List<Inntektslinje> = listOf()
        Gitt("følgende opplysninger om inntekt") { tabell: DataTable ->
            inntekstlinjer = tabell.asMaps().map {
                Inntektslinje(
                        it["inntektType"]!!,
                        BigDecimal(it["beløp"]),
                        LocalDate.parse(it["opptjeningsperiodeFom"]),
                        LocalDate.parse(it["opptjeningsperiodeTom"])
                )
            }
        }
        Når("lønnstilskudd på {int} prosent skal refunderes for periode {string} til {string}") { lønnstilskuddProsent: Int, startDatoString: String, sluttDatoString: String ->
            prosent = lønnstilskuddProsent
            if (startDatoString.isNotEmpty()) {
                startDato = LocalDate.parse(startDatoString)
            }
            if (sluttDatoString.isNotEmpty()) {
                sluttDato = LocalDate.parse(sluttDatoString)
            }

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