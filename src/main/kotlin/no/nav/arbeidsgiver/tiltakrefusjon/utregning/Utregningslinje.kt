package no.nav.arbeidsgiver.tiltakrefusjon.utregning

import com.fasterxml.jackson.annotation.JsonInclude
import no.nav.arbeidsgiver.tiltakrefusjon.utregning.Fortegn.ER_LIK
import no.nav.arbeidsgiver.tiltakrefusjon.utregning.Fortegn.MINUS
import no.nav.arbeidsgiver.tiltakrefusjon.utregning.Fortegn.PLUSS
import kotlin.math.absoluteValue

@JsonInclude(JsonInclude.Include.NON_NULL)
data class Utregningslinje(
    val type: UtregningsradType,
    val basis: Verdi,
    val fortegn: Fortegn? = null,
    val utledning: Utledning? = null,
    val utgårGrunn: UtgaarGrunn? = null
) {
    val label = type.label
    val formatertVerdi: String = basis.formatertVerdi

    infix fun ogErUtledetAv(utledning: Utledning): Utregningslinje = copy(utledning = utledning)

    infix fun utgårGrunnet(utgaarGrunn: UtgaarGrunn?): Utregningslinje = copy(utgårGrunn = utgaarGrunn)
}

/** Linjen vises uten fortegn, typisk som første linje i en gruppe. */
infix fun UtregningsradType.tilsvarer(beløp: Int) = Utregningslinje(this, beløp.kroner)

infix fun UtregningsradType.erLik(beløp: Int) = Utregningslinje(this, beløp.kroner, ER_LIK)

/**
 * Legger til et beløp som kan være positivt eller negativt. Verdien blir absolutt; fortegnet "flyttes" i eget felt
 */
infix fun UtregningsradType.leggerTil(beløp: Int) =
    Utregningslinje(this, beløp.absoluteValue.kroner, if (beløp < 0) MINUS else PLUSS)

infix fun UtregningsradType.multiplisererMed(prosent: Prosent) = Utregningslinje(this, prosent, Fortegn.MULTIPLISER)
