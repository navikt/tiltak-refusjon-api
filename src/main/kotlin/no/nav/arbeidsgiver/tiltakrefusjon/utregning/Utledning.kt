package no.nav.arbeidsgiver.tiltakrefusjon.utregning

import no.nav.arbeidsgiver.tiltakrefusjon.utils.formaterDesimal
import no.nav.arbeidsgiver.tiltakrefusjon.utils.formaterTilNorskeKroner

/**
 * Forklarer hvordan beløpet på en utregningslinje framkom. Vises i parentes etter labelen, for eksempel
 * «Feriepenger (12 %)» eller «Timelønn × antall timer (500 kr × 7,5)».
 *
 * Formene skiller seg i hvor mye av regnestykket som er synlig: [Prosent] viser bare satsen og lar
 * grunnlaget være underforstått (linjen over), mens [Timepris] viser begge operandene fordi grunnlaget
 * ikke står noe annet sted.
 */
sealed interface Utledning {
    val formatertVerdi: String
}

data class Timepris(val kronerPerTime: Int, val timer: Double) : Utledning {
    override val formatertVerdi = "${formaterTilNorskeKroner(kronerPerTime)} × ${formaterDesimal(timer)}"
    override fun toString(): String = formatertVerdi
}
