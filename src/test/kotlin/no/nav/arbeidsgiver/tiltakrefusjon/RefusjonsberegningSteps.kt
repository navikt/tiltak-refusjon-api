package no.nav.arbeidsgiver.tiltakrefusjon

import io.cucumber.datatable.DataTable
import io.cucumber.java8.No
import org.assertj.core.api.Assertions.assertThat
import java.math.BigDecimal
import java.time.LocalDate

class RefusjonsberegningSteps : No {
    init {
        var prosent = 0
        var inntekstlinjer: List<Inntektslinje> = listOf()
        Gitt("følgende opplysninger om inntekt") { tabell: DataTable ->
            inntekstlinjer = tabell.asMaps().map {
                Inntektslinje(
                        it["inntektType"]!!,
                        BigDecimal(it["beløp"]),
                        LocalDate.parse(it["opptjeningsperiodeFom"]),
                        LocalDate.parse(it["opptjeningsperiodeFom"])
                )
            }
        }
        Når("lønnstilskudd på {int} prosent skal refunderes for periode {string} til {string}") { lønnstilskuddProsent: Int, startDato: String, sluttDato: String ->
            prosent = lønnstilskuddProsent
        }
        Så("beregnes refusjon til {string} kr") { refusjon: String ->
            val beregnet = beregnRefusjon(Refusjonsgrunnlag(inntekstlinjer, prosent))
            assertThat(beregnet).isEqualByComparingTo(refusjon);
        }
    }
}