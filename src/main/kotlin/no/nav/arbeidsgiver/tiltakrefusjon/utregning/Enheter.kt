package no.nav.arbeidsgiver.tiltakrefusjon.utregning

import no.nav.arbeidsgiver.tiltakrefusjon.utils.formaterDesimal
import no.nav.arbeidsgiver.tiltakrefusjon.utils.formaterProsent
import no.nav.arbeidsgiver.tiltakrefusjon.utils.formaterTilNorskeKroner


sealed class Verdi

internal class Prosent(val desimalverdi: Double) : Verdi() {
    val formatertVerdi = formaterProsent(desimalverdi)
    override fun toString(): String = formatertVerdi
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Prosent

        return desimalverdi == other.desimalverdi
    }

    override fun hashCode(): Int {
        return desimalverdi.hashCode()
    }
}

internal val Int.prosent: Prosent
    get() = Prosent(this.toDouble().div(100))

internal val Double.prosent: Prosent
    get() = Prosent(this)

internal class Kroner(val råverdi: Int) : Verdi() {
    val formatertVerdi = "${formaterTilNorskeKroner(råverdi)}"
    override fun toString(): String = formatertVerdi
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Kroner

        return råverdi == other.råverdi
    }

    override fun hashCode(): Int {
        return råverdi.hashCode()
    }
}

internal val Int.kroner: Kroner
    get() = Kroner(this)


internal class Timelonn(val timer: Double, val timelonn: Int) : Verdi() {
    val formatertVerdi = "${formaterTilNorskeKroner(timelonn)} × ${formaterDesimal(timer)}"
    override fun toString(): String = formatertVerdi
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Timelonn

        if (timer != other.timer) return false
        if (timelonn != other.timelonn) return false

        return true
    }

    override fun hashCode(): Int {
        var result = timer.hashCode()
        result = 31 * result + timelonn
        return result
    }

}
