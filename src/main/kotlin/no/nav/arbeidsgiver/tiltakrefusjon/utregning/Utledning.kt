package no.nav.arbeidsgiver.tiltakrefusjon.utregning

import no.nav.arbeidsgiver.tiltakrefusjon.utils.formaterDesimal
import no.nav.arbeidsgiver.tiltakrefusjon.utils.formaterProsent
import no.nav.arbeidsgiver.tiltakrefusjon.utils.formaterTilNorskeKroner

/**
 * Forklarer hvordan beløpet på en utregningslinje framkom. Vises i parentes etter labelen, for eksempel
 * «Feriepenger (12 %)» eller «Timelønn × antall timer (500 kr × 7,5)».
 *
 * Formene skiller seg i hvor mye av regnestykket som er synlig: [Prosentsats] viser bare satsen og lar
 * grunnlaget være underforstått (linjen over), mens [Timepris] viser begge operandene fordi grunnlaget
 * ikke står noe annet sted.
 */
sealed interface Utledning {
    val formatertVerdi: String
}

internal data class Prosentsats(val desimalverdi: Double) : Utledning {
    override val formatertVerdi = formaterProsent(desimalverdi)
    override fun toString(): String = formatertVerdi
}

internal data class Timepris(val kronerPerTime: Int, val timer: Double) : Utledning {
    override val formatertVerdi = "${formaterTilNorskeKroner(kronerPerTime)} × ${formaterDesimal(timer)}"
    override fun toString(): String = formatertVerdi
}

internal val Int.prosentsats: Prosentsats
    get() = Prosentsats(this.toDouble().div(100))

internal val Double.prosentsats: Prosentsats
    get() = Prosentsats(this)
