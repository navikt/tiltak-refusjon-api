package no.nav.arbeidsgiver.tiltakrefusjon.utregning

import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.Refundering
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.Tiltakstype
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.kalkulerBruttoLønn
import no.nav.arbeidsgiver.tiltakrefusjon.utregning.Fortegn.ER_LIK
import no.nav.arbeidsgiver.tiltakrefusjon.utregning.Fortegn.MINUS
import no.nav.arbeidsgiver.tiltakrefusjon.utregning.Fortegn.MULTIPLISER
import no.nav.arbeidsgiver.tiltakrefusjon.utregning.Fortegn.PLUSS
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
import no.nav.arbeidsgiver.tiltakrefusjon.utregning.UtregningsradType.TIDLIGERE_UTBETALT
import no.nav.arbeidsgiver.tiltakrefusjon.utregning.UtregningsradType.TILSKUDDSPROSENT
import no.nav.arbeidsgiver.tiltakrefusjon.utregning.UtregningsradType.TIMELONN_X_TIMER
import kotlin.math.abs
import kotlin.math.roundToInt

internal infix fun UtregningsradType.erLik(kroner: Kroner): Utregningslinje =
    Utregningslinje(this, kroner, ER_LIK)
internal infix fun UtregningsradType.tilsvarer(kroner: Kroner) = Utregningslinje(this, kroner)

private fun pluss(
    label: UtregningsradType,
    kroner: Kroner
): Utregningslinje = Utregningslinje(label, abs(kroner.råverdi).kroner, if (kroner.råverdi < 0) MINUS else PLUSS)

private fun multipliser(
    label: UtregningsradType,
    kroner: Prosent
): Utregningslinje = Utregningslinje(label, kroner, MULTIPLISER)

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
            Utregningslinje(
                TIMELONN_X_TIMER,
                beregning.lønn.kroner
            ).medSats(Timelonn(tilskuddsgrunnlag.mentorAntallTimer ?: 0.0, tilskuddsgrunnlag.mentorTimelonn ?: 0)),
            pluss(FERIEPENGER, beregning.feriepenger.kroner)
                .medSats(tilskuddsgrunnlag.feriepengerSats.prosent),
            pluss(OBLIGATORISK_TJENESTEPENSJON, beregning.tjenestepensjon.kroner)
                .medSats(tilskuddsgrunnlag.otpSats.prosent),
            pluss(ARBEIDSGIVERAVGIFT, beregning.arbeidsgiveravgift.kroner)
                .medSats(tilskuddsgrunnlag.arbeidsgiveravgiftSats.prosent)
        )
    )
    val resultatMinusDelvisPeriode = if (beregning.sumUtgifter != beregning.refusjonsbeløp)
        Utregningsgruppe(
            listOf(
                SUM_TILSKUDD_FOR_EN_MND erLik beregning.sumUtgifter.kroner,
                pluss(
                    REDUKSJON_FOR_DELVIS_PERIODE, (beregning.refusjonsbeløp - beregning.sumUtgifter).kroner
                )
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
    val lønn = if (korrigertBruttoLønn != null) minOf(
        korrigertBruttoLønn,
        kalkulertBruttoLønn
    ) else kalkulertBruttoLønn

    val sosialeUtgifter = listOfNotNull(
        Utregningslinje(BRUTTOLONN_I_PERIODEN, lønn.kroner),
        if (beregning.fratrekkLønnFerie != 0)
            pluss(FERIETREKK, beregning.fratrekkLønnFerie.kroner)
        else null,
        pluss(FERIEPENGER, beregning.feriepenger.kroner)
            .medSats(tilskuddsgrunnlag.feriepengerSats.prosent),
        pluss(OBLIGATORISK_TJENESTEPENSJON, beregning.tjenestepensjon.kroner)
            .medSats(tilskuddsgrunnlag.otpSats.prosent),
        pluss(ARBEIDSGIVERAVGIFT, beregning.arbeidsgiveravgift.kroner)
            .medSats(tilskuddsgrunnlag.arbeidsgiveravgiftSats.prosent),
    )

    val tidligereRefundertBolk = if (beregning.tidligereRefundertBeløp != 0)
        listOf(
            SUM_BRUTTO_LONNSUTGIFTER erLik beregning.sumUtgifter.kroner,
            pluss(UtregningsradType.TIDLIGERE_REFUNDERBART_FOR_FRAVAER, (-1 * beregning.tidligereRefundertBeløp).kroner)
        )
    else emptyList()

    val refusjonsgrunnlagsbolk = listOf(
        REFUSJONSGRUNNLAG erLik beregning.sumUtgifterFratrukketRefundertBeløp.kroner,
        multipliser(
            TILSKUDDSPROSENT,
            refundering.refusjonsgrunnlag.tilskuddsgrunnlag.lønnstilskuddsprosent.prosent
        )
    )


    val overskriderMaksbeløp = beregning.overFemGrunnbeløp == true || beregning.overTilskuddsbeløp
    val harMinusbelop = refundering.refusjonsgrunnlag.forrigeRefusjonMinusBeløp < 0

    var justertBeregningsgruppe: Utregningsgruppe? = null
    if (beregning.refusjonsbeløp != beregning.beregnetBeløp) {
        // Refusjonsbeløp og beregnet beløp er ikke like, som enten betyr at vi har gått over maksbeløper (tilskudd eller 5G),
        // eller at det fins et minusbeløp eller "tidligere utbetalt" (altså vi utfører en korrigering), eventuelt alt
        // på en gang.
        val overTilskuddsbeløp = beregning.overTilskuddsbeløp
        val over5g = beregning.overFemGrunnbeløp == true
        val minusbeløp = refundering.refusjonsgrunnlag.forrigeRefusjonMinusBeløp
        val tidligereUtbetalt = beregning.tidligereUtbetalt
        val tilskuddsbeløp = refundering.refusjonsgrunnlag.tilskuddsgrunnlag.tilskuddsbeløp.kroner

        justertBeregningsgruppe = Utregningsgruppe(
            listOfNotNull(
                (BEREGNET_BELOP erLik beregning.beregnetBeløp.kroner).apply { utgårHvis(overTilskuddsbeløp) },
                if (overTilskuddsbeløp && !over5g)
                    AVTALT_BELOP tilsvarer tilskuddsbeløp
                else null,
                if (minusbeløp != 0)
                    pluss(
                        RESTERENDE_FRATREKK_FOR_FERIE_FRA_TIDLIGERE_REFUSJONER,
                        minusbeløp.kroner
                    )
                else null,
                if (over5g && minusbeløp != 0) {
                    (BEREGNET_BELOP_ETTER_RESTTREKK erLik (beregning.beregnetBeløp + minusbeløp).kroner).apply { utgår = true }
                } else null,
                if (over5g) {
                    AVTALT_BELOP_REST_5G tilsvarer (beregning.refusjonsbeløp + tidligereUtbetalt).kroner
                } else null,
                if (beregning.tidligereUtbetalt != 0) {
                    pluss(TIDLIGERE_UTBETALT, (-tidligereUtbetalt).kroner)
                } else null
            )
        )
    }

    return Utregning(
        listOfNotNull(
            Utregningsgruppe(
                sosialeUtgifter
            ),
            Utregningsgruppe(
                tidligereRefundertBolk
            ),
            Utregningsgruppe(
                refusjonsgrunnlagsbolk
            ),
            justertBeregningsgruppe,
            Utregningsgruppe(
                listOf(
                    REFUSJONSBELØP_TIL_UTBETALING erLik beregning.refusjonsbeløp.kroner
                )
            )
        ).filter { it.rader.isNotEmpty() }
    )
}
