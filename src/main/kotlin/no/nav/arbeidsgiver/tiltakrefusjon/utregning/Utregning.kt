package no.nav.arbeidsgiver.tiltakrefusjon.utregning

import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.Beregning
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.Refundering
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.Tiltakstype
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.kalkulerBruttoLønn
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
import no.nav.arbeidsgiver.tiltakrefusjon.utregning.UtregningsradType.REFUSJONSBELØP_TIL_UTBETALING
import no.nav.arbeidsgiver.tiltakrefusjon.utregning.UtregningsradType.REFUSJONSGRUNNLAG
import no.nav.arbeidsgiver.tiltakrefusjon.utregning.UtregningsradType.RESTERENDE_FRATREKK_FOR_FERIE_FRA_TIDLIGERE_REFUSJONER
import no.nav.arbeidsgiver.tiltakrefusjon.utregning.UtregningsradType.SUM_BRUTTO_LONNSUTGIFTER
import no.nav.arbeidsgiver.tiltakrefusjon.utregning.UtregningsradType.SUM_TILSKUDD_FOR_EN_MND
import no.nav.arbeidsgiver.tiltakrefusjon.utregning.UtregningsradType.TIDLIGERE_REFUNDERBART_FOR_FRAVAER
import no.nav.arbeidsgiver.tiltakrefusjon.utregning.UtregningsradType.TIDLIGERE_UTBETALT
import no.nav.arbeidsgiver.tiltakrefusjon.utregning.UtregningsradType.TILSKUDDSPROSENT
import no.nav.arbeidsgiver.tiltakrefusjon.utregning.UtregningsradType.TIMELONN_X_TIMER
import kotlin.math.roundToInt

data class Utregningsgruppe(val rader: List<Utregningslinje>)

data class Utregning(
    val grupperinger: List<Utregningsgruppe>
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

    val sosialeUtgifter = Utregningsgruppe(
        listOf(
            TIMELONN_X_TIMER tilsvarer beregning.lønn.kroner medSats Timelonn(
                tilskuddsgrunnlag.mentorAntallTimer ?: 0.0,
                tilskuddsgrunnlag.mentorTimelonn ?: 0
            ),
            FERIEPENGER pluss beregning.feriepenger.kroner medSats tilskuddsgrunnlag.feriepengerSats.prosent,
            OBLIGATORISK_TJENESTEPENSJON pluss beregning.tjenestepensjon.kroner medSats tilskuddsgrunnlag.otpSats.prosent,
            ARBEIDSGIVERAVGIFT pluss beregning.arbeidsgiveravgift.kroner medSats tilskuddsgrunnlag.arbeidsgiveravgiftSats.prosent
        )
    )

    val resultatMinusDelvisPeriode = if (beregning.sumUtgifter != beregning.refusjonsbeløp)
        Utregningsgruppe(
            listOf(
                SUM_TILSKUDD_FOR_EN_MND erLik beregning.sumUtgifter.kroner,
                REDUKSJON_FOR_DELVIS_PERIODE minus (beregning.sumUtgifter - beregning.refusjonsbeløp).kroner
            )
        ) else null

    return Utregning(
        grupperinger = listOfNotNull(
            sosialeUtgifter,
            resultatMinusDelvisPeriode,
            Utregningsgruppe(listOf(REFUSJONSBELØP_TIL_UTBETALING erLik beregning.refusjonsbeløp.kroner))
        )
    )
}

private fun tilskuddsutregning(refundering: Refundering): Utregning? {
    val beregning = refundering.refusjonsgrunnlag.beregning ?: return null
    val tilskuddsgrunnlag = refundering.refusjonsgrunnlag.tilskuddsgrunnlag
    val inntekter = refundering.refusjonsgrunnlag.inntektsgrunnlag?.inntekter ?: emptyList()
    val kalkulertBruttoLønn = kalkulerBruttoLønn(inntekter).roundToInt()
    val korrigertBruttoLønn = refundering.refusjonsgrunnlag.endretBruttoLønn
    val lønn = if (korrigertBruttoLønn != null) minOf(korrigertBruttoLønn, kalkulertBruttoLønn)
    else kalkulertBruttoLønn

    val sosialeUtgifter = listOfNotNull(
        BRUTTOLONN_I_PERIODEN tilsvarer lønn.kroner,
        if (beregning.fratrekkLønnFerie != 0)
            FERIETREKK pluss beregning.fratrekkLønnFerie.kroner
        else null,
        FERIEPENGER pluss beregning.feriepenger.kroner medSats tilskuddsgrunnlag.feriepengerSats.prosent,
        OBLIGATORISK_TJENESTEPENSJON pluss beregning.tjenestepensjon.kroner medSats tilskuddsgrunnlag.otpSats.prosent,
        ARBEIDSGIVERAVGIFT pluss beregning.arbeidsgiveravgift.kroner medSats tilskuddsgrunnlag.arbeidsgiveravgiftSats.prosent
    )

    val tidligereRefundertBolk = if (beregning.tidligereRefundertBeløp != 0)
        listOf(
            SUM_BRUTTO_LONNSUTGIFTER erLik beregning.sumUtgifter.kroner,
            TIDLIGERE_REFUNDERBART_FOR_FRAVAER minus beregning.tidligereRefundertBeløp.kroner
        )
    else emptyList()

    val refusjonsgrunnlagsbolk = listOf(
        REFUSJONSGRUNNLAG erLik beregning.sumUtgifterFratrukketRefundertBeløp.kroner,
        TILSKUDDSPROSENT multiplisertMed tilskuddsgrunnlag.lønnstilskuddsprosent.prosent
    )

    return Utregning(
        listOfNotNull(
            Utregningsgruppe(sosialeUtgifter),
            Utregningsgruppe(tidligereRefundertBolk),
            Utregningsgruppe(refusjonsgrunnlagsbolk),
            justertBeregning(
                beregning = beregning,
                tilskuddsbeløp = tilskuddsgrunnlag.tilskuddsbeløp,
                minusbeløp = refundering.refusjonsgrunnlag.forrigeRefusjonMinusBeløp
            ),
            Utregningsgruppe(listOf(REFUSJONSBELØP_TIL_UTBETALING erLik beregning.refusjonsbeløp.kroner))
        ).filter { it.rader.isNotEmpty() }
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
    val tidligereUtbetalt = beregning.tidligereUtbetalt
    val harMinusbeløp = minusbeløp != 0

    // Uten minusbeløp er det 5G-linjen som forklarer resultatet, og avtalt beløp blir et mellomledd
    // som ikke tilfører noe. Med minusbeløp trengs avtalt beløp som grunnlag for fratrekket.
    val femGErstatterAvtaltBeløp = over5g && !harMinusbeløp

    // Samme grunnlag som Refusjonsberegner bruker: er beregnet beløp kappet til avtalt tilskudd,
    // er det tilskuddsbeløpet minusbeløpet trekkes fra.
    val beløpFørMinustrekk = if (overTilskuddsbeløp) tilskuddsbeløp else beregning.beregnetBeløp

    return Utregningsgruppe(
        listOfNotNull(
            BEREGNET_BELOP erLik beregning.beregnetBeløp.kroner
                    utgårHvis (overTilskuddsbeløp || femGErstatterAvtaltBeløp),
            if (overTilskuddsbeløp && !femGErstatterAvtaltBeløp)
                AVTALT_BELOP tilsvarer tilskuddsbeløp.kroner
            else null,
            if (harMinusbeløp)
                RESTERENDE_FRATREKK_FOR_FERIE_FRA_TIDLIGERE_REFUSJONER pluss minusbeløp.kroner
            else null,
            if (over5g && harMinusbeløp)
                (BEREGNET_BELOP_ETTER_RESTTREKK erLik (beløpFørMinustrekk + minusbeløp).kroner).utgår()
            else null,
            if (over5g)
                AVTALT_BELOP_REST_5G tilsvarer (beregning.refusjonsbeløp + tidligereUtbetalt).kroner
            else null,
            if (tidligereUtbetalt != 0)
                TIDLIGERE_UTBETALT minus tidligereUtbetalt.kroner
            else null
        )
    )
}
