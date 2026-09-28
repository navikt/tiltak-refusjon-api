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
    val utledning: Utledning? = null,
    val utgårFordi: Maksbeløp? = null
) {
    val label = type.label

    infix fun utledesAv(utledning: Utledning): Utregningslinje = copy(utledning = utledning)

    /**
     * Markerer linjen som overstrøket fordi [maksbeløp] ga et lavere tak. Er [maksbeløp] null, gjelder
     * linjen og blir stående.
     */
    infix fun utgårFordi(maksbeløp: Maksbeløp?): Utregningslinje = copy(utgårFordi = maksbeløp)
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
