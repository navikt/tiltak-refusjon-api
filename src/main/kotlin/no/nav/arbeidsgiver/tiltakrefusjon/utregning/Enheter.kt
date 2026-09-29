package no.nav.arbeidsgiver.tiltakrefusjon.utregning

import no.nav.arbeidsgiver.tiltakrefusjon.utils.formaterProsent
import no.nav.arbeidsgiver.tiltakrefusjon.utils.formaterTilNorskeKroner

sealed interface Verdi {
    val formatertVerdi: String
}

/**
 * En andel, for eksempel 0,12. [Prosent] fyller to roller: som [Verdi] på linjen «Tilskuddsprosent ×»,
 * og som [Utledning] på linjene den satsen er brukt på. Derfor er den begge deler.
 */
data class Prosent(val desimalverdi: Double) : Verdi, Utledning {
    override val formatertVerdi = formaterProsent(desimalverdi)
    override fun toString(): String = formatertVerdi
}

/** Tar en sats i prosentpoeng, for eksempel `50.prosent` gir 50 %. */
val Int.prosent: Prosent
    get() = Prosent(this.toDouble().div(100))

/** Tar en andel, for eksempel `0.12.prosent` gir 12 %. */
val Double.prosent: Prosent
    get() = Prosent(this)

data class Kroner(val beløp: Int) : Verdi {
    override val formatertVerdi = formaterTilNorskeKroner(beløp)
    override fun toString() = formatertVerdi
}

val Int.kroner: Kroner
    get() = Kroner(this)
