package no.nav.arbeidsgiver.tiltakrefusjon.utregning

import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.Beregning
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.Refundering
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.Tiltakstype
import no.nav.arbeidsgiver.tiltakrefusjon.utregning.UtgaarGrunn.OVER_AVTALT_TILSKUDD
import no.nav.arbeidsgiver.tiltakrefusjon.utregning.UtgaarGrunn.OVER_FEM_GRUNNBELOP
import no.nav.arbeidsgiver.tiltakrefusjon.utregning.UtregningsradType.ARBEIDSGIVERAVGIFT
import no.nav.arbeidsgiver.tiltakrefusjon.utregning.UtregningsradType.AVTALT_BELOP
import no.nav.arbeidsgiver.tiltakrefusjon.utregning.UtregningsradType.AVTALT_BELOP_REST_5G
import no.nav.arbeidsgiver.tiltakrefusjon.utregning.UtregningsradType.BEREGNET_BELOP
import no.nav.arbeidsgiver.tiltakrefusjon.utregning.UtregningsradType.BEREGNET_BELOP_ETTER_RESTTREKK
import no.nav.arbeidsgiver.tiltakrefusjon.utregning.UtregningsradType.BRUTTOLONN_I_PERIODEN
import no.nav.arbeidsgiver.tiltakrefusjon.utregning.UtregningsradType.FERIEPENGER
import no.nav.arbeidsgiver.tiltakrefusjon.utregning.UtregningsradType.FERIETREKK
import no.nav.arbeidsgiver.tiltakrefusjon.utregning.UtregningsradType.OBLIGATORISK_TJENESTEPENSJON
import no.nav.arbeidsgiver.tiltakrefusjon.utregning.UtregningsradType.REDUKSJON_FOR_DELVIS_PERIODE
import no.nav.arbeidsgiver.tiltakrefusjon.utregning.UtregningsradType.REFUSJONSBELOP_TIL_UTBETALING
import no.nav.arbeidsgiver.tiltakrefusjon.utregning.UtregningsradType.REFUSJONSGRUNNLAG
import no.nav.arbeidsgiver.tiltakrefusjon.utregning.UtregningsradType.RESTERENDE_FRATREKK_FOR_FERIE_FRA_TIDLIGERE_REFUSJONER
import no.nav.arbeidsgiver.tiltakrefusjon.utregning.UtregningsradType.SUM_BRUTTO_LONNSUTGIFTER
import no.nav.arbeidsgiver.tiltakrefusjon.utregning.UtregningsradType.SUM_TILSKUDD_FOR_EN_MND
import no.nav.arbeidsgiver.tiltakrefusjon.utregning.UtregningsradType.TIDLIGERE_REFUNDERBART_FOR_FRAVAER
import no.nav.arbeidsgiver.tiltakrefusjon.utregning.UtregningsradType.TIDLIGERE_UTBETALT
import no.nav.arbeidsgiver.tiltakrefusjon.utregning.UtregningsradType.TILSKUDDSPROSENT
import no.nav.arbeidsgiver.tiltakrefusjon.utregning.UtregningsradType.TIMELONN_X_TIMER

typealias Utregningsgruppe = List<Utregningslinje>

data class Utregning(
    val grupper: List<Utregningsgruppe>,
) {
    companion object {
        fun from(refundering: Refundering) = when (refundering.tiltakstype()) {
            Tiltakstype.MENTOR -> mentorUtregning(refundering)
            Tiltakstype.FIREARIG_LONNSTILSKUDD,
            Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD,
            Tiltakstype.SOMMERJOBB,
            Tiltakstype.VARIG_LONNSTILSKUDD -> tilskuddsutregning(refundering)

            Tiltakstype.VTAO -> null
        }
    }
}

private fun mentorUtregning(refundering: Refundering): Utregning? {
    val beregning = refundering.refusjonsgrunnlag.beregning ?: return null
    val tilskuddsgrunnlag = refundering.refusjonsgrunnlag.tilskuddsgrunnlag

    val sosialeUtgifter = listOf(
        TIMELONN_X_TIMER tilsvarer beregning.lønn ogErUtledetAv Timepris(
            kronerPerTime = tilskuddsgrunnlag.mentorTimelonn ?: 0,
            timer = tilskuddsgrunnlag.mentorAntallTimer ?: 0.0
        ),
        FERIEPENGER leggerTil beregning.feriepenger ogErUtledetAv tilskuddsgrunnlag.feriepengerSats.prosent,
        OBLIGATORISK_TJENESTEPENSJON leggerTil beregning.tjenestepensjon ogErUtledetAv tilskuddsgrunnlag.otpSats.prosent,
        ARBEIDSGIVERAVGIFT leggerTil beregning.arbeidsgiveravgift ogErUtledetAv tilskuddsgrunnlag.arbeidsgiveravgiftSats.prosent
    )

    val resultatMinusDelvisPeriode = if (beregning.sumUtgifter != beregning.refusjonsbeløp)
        listOf(
            SUM_TILSKUDD_FOR_EN_MND erLik beregning.sumUtgifter,
            REDUKSJON_FOR_DELVIS_PERIODE leggerTil (beregning.refusjonsbeløp - beregning.sumUtgifter)
        ) else null

    val oppsummering = listOf(REFUSJONSBELOP_TIL_UTBETALING erLik beregning.refusjonsbeløp)

    return utregning(
        sosialeUtgifter,
        resultatMinusDelvisPeriode,
        oppsummering
    )
}

private fun tilskuddsutregning(refundering: Refundering): Utregning? {
    val beregning = refundering.refusjonsgrunnlag.beregning ?: return null
    val tilskuddsgrunnlag = refundering.refusjonsgrunnlag.tilskuddsgrunnlag
    val fratrekkGrunnetfravaer = (beregning.tidligereRefundertBeløp * -1)

    val sosialeUtgifter = listOfNotNull(
        BRUTTOLONN_I_PERIODEN tilsvarer beregning.lønn,
        if (beregning.fratrekkLønnFerie != 0)
            FERIETREKK leggerTil beregning.fratrekkLønnFerie
        else null,
        FERIEPENGER leggerTil beregning.feriepenger ogErUtledetAv tilskuddsgrunnlag.feriepengerSats.prosent,
        OBLIGATORISK_TJENESTEPENSJON leggerTil beregning.tjenestepensjon ogErUtledetAv tilskuddsgrunnlag.otpSats.prosent,
        ARBEIDSGIVERAVGIFT leggerTil beregning.arbeidsgiveravgift ogErUtledetAv tilskuddsgrunnlag.arbeidsgiveravgiftSats.prosent
    )

    val tidligereRefundert = if (beregning.tidligereRefundertBeløp != 0)
        listOf(
            SUM_BRUTTO_LONNSUTGIFTER erLik beregning.sumUtgifter,
            TIDLIGERE_REFUNDERBART_FOR_FRAVAER leggerTil fratrekkGrunnetfravaer
        )
    else null

    val refusjonsgrunnlag = listOf(
        REFUSJONSGRUNNLAG erLik beregning.sumUtgifterFratrukketRefundertBeløp,
        TILSKUDDSPROSENT multiplisererMed tilskuddsgrunnlag.lønnstilskuddsprosent.prosent
    )

    val oppsummering = listOf(REFUSJONSBELOP_TIL_UTBETALING erLik beregning.refusjonsbeløp)

    return utregning(
        sosialeUtgifter,
        tidligereRefundert,
        refusjonsgrunnlag,
        justertBeregning(
            beregning = beregning,
            tilskuddsbeløp = tilskuddsgrunnlag.tilskuddsbeløp,
            minusbeløp = refundering.refusjonsgrunnlag.forrigeRefusjonMinusBeløp
        ),
        oppsummering
    )
}

/**
 * Gruppa som forklarer hvorfor refusjonsbeløpet avviker fra det beregnede beløpet. Avviket oppstår når vi
 * har gått over et maksbeløp (avtalt tilskudd eller 5G), når det fins et minusbeløp fra en tidligere
 * refusjon, eller når vi korrigerer og må trekke fra det som alt er utbetalt. Flere av dem kan slå inn
 * samtidig. Er beløpene like, trengs ingen forklaring og gruppa utelates.
 *
 * Linjene under står i samme rekkefølge som de vises for arbeidsgiver.
 */
private fun justertBeregning(
    beregning: Beregning,
    tilskuddsbeløp: Int,
    minusbeløp: Int
): Utregningsgruppe? {
    if (beregning.refusjonsbeløp == beregning.beregnetBeløp) return null

    val overTilskuddsbeløp = beregning.overTilskuddsbeløp
    val over5g = beregning.overFemGrunnbeløp == true
    val tidligereUtbetalt = beregning.tidligereUtbetalt * -1
    val harMinusbeløp = minusbeløp != 0

    // Uten minusbeløp er det 5G-linjen som forklarer resultatet, og avtalt beløp blir et mellomledd
    // som ikke tilfører noe. Med minusbeløp trengs avtalt beløp som grunnlag for fratrekket.
    val femGErstatterAvtaltBeløp = over5g && !harMinusbeløp

    // Taket som faktisk erstatter det beregnede beløpet. 5G har forrang: når begge slår inn uten
    // minusbeløp, utelates avtalt beløp-linjen, og det er 5G-linjen som overtar.
    val beregnetBeløpErstattesAv = when {
        femGErstatterAvtaltBeløp -> OVER_FEM_GRUNNBELOP
        overTilskuddsbeløp -> OVER_AVTALT_TILSKUDD
        else -> null
    }

    // Samme grunnlag som Refusjonsberegner bruker: er beregnet beløp kappet til avtalt tilskudd,
    // er det tilskuddsbeløpet minusbeløpet trekkes fra.
    val beløpFørMinustrekk = if (overTilskuddsbeløp) tilskuddsbeløp else beregning.beregnetBeløp

    return listOfNotNull(
        BEREGNET_BELOP erLik beregning.beregnetBeløp utgårGrunnet beregnetBeløpErstattesAv,
        if (overTilskuddsbeløp && !femGErstatterAvtaltBeløp)
            AVTALT_BELOP tilsvarer tilskuddsbeløp
        else null,
        if (harMinusbeløp)
            RESTERENDE_FRATREKK_FOR_FERIE_FRA_TIDLIGERE_REFUSJONER leggerTil minusbeløp
        else null,
        if (over5g && harMinusbeløp)
            BEREGNET_BELOP_ETTER_RESTTREKK erLik (beløpFørMinustrekk + minusbeløp) utgårGrunnet OVER_FEM_GRUNNBELOP
        else null,
        if (over5g)
            AVTALT_BELOP_REST_5G tilsvarer (beregning.refusjonsbeløp - tidligereUtbetalt)
        else null,
        if (tidligereUtbetalt != 0)
            TIDLIGERE_UTBETALT leggerTil tidligereUtbetalt
        else null
    )
}

fun utregning(vararg grupper: Utregningsgruppe?): Utregning = Utregning(
    grupper.filterNotNull().toList()
)
