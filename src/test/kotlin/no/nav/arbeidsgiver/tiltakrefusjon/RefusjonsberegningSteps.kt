package no.nav.arbeidsgiver.tiltakrefusjon

import io.cucumber.java8.No
import no.nav.arbeidsgiver.tiltakrefusjon.domain.Refusjonsgrunnlag
import org.assertj.core.api.Assertions.assertThat

class RefusjonsberegningSteps : No {
    init {
        var grunnlag = 0
        var prosent = 0
        Gitt("følgende opplysninger om inntekt") {
            grunnlag = 10000
        }
        Når("lønnstilskudd på {int} prosent skal refunderes for periode {string} til {string}") { lønnstilskuddProsent: Int, startDato: String, sluttDato: String ->
            prosent = lønnstilskuddProsent
        }
        Så("beregnes refusjon til {int} kr") { refusjon: Int ->
            val beregnet = beregnRefusjon(Refusjonsgrunnlag(grunnlag, prosent))
            assertThat(beregnet).isEqualTo(refusjon);
        }
    }
}