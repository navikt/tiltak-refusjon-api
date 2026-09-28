package no.nav.arbeidsgiver.tiltakrefusjon.utregning

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
            TIDLIGERE_REFUNDERBART_FOR_FRAVAER pluss (-beregning.tidligereRefundertBeløp).kroner
        )
    else emptyList()

    val refusjonsgrunnlagsbolk = listOf(
        REFUSJONSGRUNNLAG erLik beregning.sumUtgifterFratrukketRefundertBeløp.kroner,
        TILSKUDDSPROSENT multiplisertMed tilskuddsgrunnlag.lønnstilskuddsprosent.prosent
    )

    // Refusjonsbeløp og beregnet beløp er ikke like, som enten betyr at vi har gått over maksbeløper (tilskudd eller 5G),
    // eller at det fins et minusbeløp eller "tidligere utbetalt" (altså vi utfører en korrigering), eventuelt alt
    // på en gang.
    val justertBeregningsgruppe = if (beregning.refusjonsbeløp != beregning.beregnetBeløp) {
        val overTilskuddsbeløp = beregning.overTilskuddsbeløp
        val over5g = beregning.overFemGrunnbeløp == true
        val minusbeløp = refundering.refusjonsgrunnlag.forrigeRefusjonMinusBeløp
        val tidligereUtbetalt = beregning.tidligereUtbetalt

        val utgaarFoerMinusbelop = overTilskuddsbeløp || (over5g && minusbeløp == 0)

        val beregnetMedMinusbelop = listOfNotNull(
            BEREGNET_BELOP erLik beregning.beregnetBeløp.kroner utgårHvis utgaarFoerMinusbelop,
            if (overTilskuddsbeløp)
                AVTALT_BELOP tilsvarer tilskuddsgrunnlag.tilskuddsbeløp.kroner
            else null,
            if (minusbeløp != 0)
                RESTERENDE_FRATREKK_FOR_FERIE_FRA_TIDLIGERE_REFUSJONER pluss minusbeløp.kroner
            else null,
        )

        val minusTidligereUtbetalt = listOfNotNull(
            if (tidligereUtbetalt != 0)
                TIDLIGERE_UTBETALT minus tidligereUtbetalt.kroner
            else null
        )

        // Hvis beløp overskrider 5g, skal reduksjonslinje vises. Dersom et minusbeløp også eksisterer for refusjonen,
        // må vi i tillegg vise resultat før reduksjon (ellers vil "BEREGNET_BELØP fungere som
        val over5G = if (over5g)
            listOfNotNull(
                if (minusbeløp != 0) (BEREGNET_BELOP_ETTER_RESTTREKK erLik (beregning.beregnetBeløp + minusbeløp).kroner).utgår()
                else null,
                AVTALT_BELOP_REST_5G tilsvarer (beregning.refusjonsbeløp + tidligereUtbetalt).kroner
            ) else emptyList()

        Utregningsgruppe(
            beregnetMedMinusbelop + over5G + minusTidligereUtbetalt
        )
    } else null

    return Utregning(
        listOfNotNull(
            Utregningsgruppe(sosialeUtgifter),
            Utregningsgruppe(tidligereRefundertBolk),
            Utregningsgruppe(refusjonsgrunnlagsbolk),
            justertBeregningsgruppe,
            Utregningsgruppe(listOf(REFUSJONSBELØP_TIL_UTBETALING erLik beregning.refusjonsbeløp.kroner))
        ).filter { it.rader.isNotEmpty() }
    )
}
