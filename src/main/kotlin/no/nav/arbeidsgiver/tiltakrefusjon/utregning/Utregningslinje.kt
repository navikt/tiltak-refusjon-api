package no.nav.arbeidsgiver.tiltakrefusjon.utregning

import com.fasterxml.jackson.annotation.JsonInclude
import no.nav.arbeidsgiver.tiltakrefusjon.utregning.Fortegn.ER_LIK
import no.nav.arbeidsgiver.tiltakrefusjon.utregning.Fortegn.MINUS
import no.nav.arbeidsgiver.tiltakrefusjon.utregning.Fortegn.MULTIPLISER
import no.nav.arbeidsgiver.tiltakrefusjon.utregning.Fortegn.PLUSS
import kotlin.math.absoluteValue

@JsonInclude(JsonInclude.Include.NON_NULL)
data class Utregningslinje(
    val type: UtregningsradType,
    val verdi: Verdi,
    val fortegn: Fortegn? = null,
    val sats: Verdi? = null,
    val utgår: Boolean? = null
) {
    val label = type.label

    infix fun medSats(sats: Verdi): Utregningslinje = copy(sats = sats)

    fun utgår(): Utregningslinje = copy(utgår = true)

    infix fun utgårHvis(betingelse: Boolean): Utregningslinje = if (betingelse) utgår() else this
}

/** Linjen vises uten fortegn, typisk som første linje i en gruppe. */
internal infix fun UtregningsradType.tilsvarer(verdi: Verdi) = Utregningslinje(this, verdi)

internal infix fun UtregningsradType.erLik(kroner: Kroner) = Utregningslinje(this, kroner, ER_LIK)

/**
 * Legger til et beløp som kan ha begge fortegn. Linjen vises med `+` for positive beløp og `-` for
 * negative, og selve beløpet vises alltid uten fortegn.
 */
internal infix fun UtregningsradType.pluss(kroner: Kroner) =
    Utregningslinje(this, kroner.råverdi.absoluteValue.kroner, if (kroner.råverdi < 0) MINUS else PLUSS)

/** Trekker fra et beløp. Linjen vises alltid med `-`, uavhengig av fortegnet til [kroner]. */
internal infix fun UtregningsradType.minus(kroner: Kroner) =
    Utregningslinje(this, kroner.råverdi.absoluteValue.kroner, if (kroner.råverdi >= 0) MINUS else PLUSS)

internal infix fun UtregningsradType.multiplisertMed(prosent: Prosent) = Utregningslinje(this, prosent, MULTIPLISER)
