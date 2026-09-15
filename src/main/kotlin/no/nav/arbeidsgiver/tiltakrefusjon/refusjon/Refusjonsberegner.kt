package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import no.nav.arbeidsgiver.tiltakrefusjon.grunnbelop.Grunnbelop
import java.time.LocalDate
import kotlin.math.roundToInt

private fun fastBeløpBeregning(
    tilskuddsgrunnlag: Tilskuddsgrunnlag,
    utbetaltIRefusjonSomSkalKorrigeres: Int,
    korriger: Boolean = false
): Beregning {
    val beregnetBeløp = if (korriger) 0 else tilskuddsgrunnlag.tilskuddsbeløp

    return Beregning(
        lønn = 0,
        lønnFratrukketFerie = 0,
        feriepenger = 0,
        tjenestepensjon = 0,
        arbeidsgiveravgift = 0,
        sumUtgifter = 0,
        beregnetBeløp = beregnetBeløp,
        refusjonsbeløp = beregnetBeløp - utbetaltIRefusjonSomSkalKorrigeres,
        overTilskuddsbeløp = false,
        tidligereUtbetalt = utbetaltIRefusjonSomSkalKorrigeres,
        fratrekkLønnFerie = 0,
        tidligereRefundertBeløp = 0,
        overFemGrunnbeløp = false,
        sumUtgifterFratrukketRefundertBeløp = 0
    )
}

private fun mentorBeregning(tilskuddsgrunnlag: Tilskuddsgrunnlag): Beregning {
    val mentorAntallTimer: Double? = tilskuddsgrunnlag.mentorAntallTimer
    val mentorTimelonn: Int? = tilskuddsgrunnlag.mentorTimelonn
    if (mentorTimelonn == null || mentorAntallTimer == null) {
        throw RuntimeException(
            "Tilskuddsgrunnlag ${tilskuddsgrunnlag.id} mangler verdi for mentorAntallTimer eller mentorTimelonn!" +
                    "Har mentorberegning blitt kalt uten å først sjekke om alle felter er fylt ut?"
        )
    }
    val lonn = mentorAntallTimer * mentorTimelonn
    val feriepenger = lonn * tilskuddsgrunnlag.feriepengerSats
    val tjenestepensjon = (lonn + feriepenger) * tilskuddsgrunnlag.otpSats
    val arbeidsgiveravgift = (lonn + tjenestepensjon + feriepenger) * tilskuddsgrunnlag.arbeidsgiveravgiftSats
    val beregnetBeløp = (lonn + tjenestepensjon + feriepenger + arbeidsgiveravgift).roundToInt()

    return Beregning(
        lønn = lonn.roundToInt(),
        lønnFratrukketFerie = 0,
        feriepenger = feriepenger.roundToInt(),
        tjenestepensjon = tjenestepensjon.roundToInt(),
        arbeidsgiveravgift = arbeidsgiveravgift.roundToInt(),
        sumUtgifter = beregnetBeløp,
        beregnetBeløp = tilskuddsgrunnlag.tilskuddsbeløp,
        refusjonsbeløp = tilskuddsgrunnlag.tilskuddsbeløp,
        overTilskuddsbeløp = false,
        tidligereUtbetalt = 0,
        fratrekkLønnFerie = 0,
        tidligereRefundertBeløp = 0,
        overFemGrunnbeløp = false,
        sumUtgifterFratrukketRefundertBeløp = 0
    )
}

/**
 * Utfør en beregning på refusjon (eller korreksjon). Forskjellen på en korreksjon og refusjon er at en
 * refusjon alltid har 0 for "utbetaltIRefusjonSomSkalKorrigeres".
 *
 * ## Viktige detaljer
 * 1. Vi trekker fra minusbeløp ETTER reduksjon ned til maks tilskuddsbeløp. Hensikten er at minusbeløpet
 * skal trekkes fra det som normalt vil være en sluttsum. Minusbeløp trekkes _før_ 5G fordi 5G-grensen skal være
 * en absolutt grense på utbetalinger på tvers av tiltaket.
 * 2. Tidligere refusjonsbeløp trekkes helt til sist, etter 5G-reduksjon! Poenget her er at beløpet som ligger i
 * `utbetaltIRefusjonSomSkalKorrigeres`-feltet er den absolutt endelige summen som ble beregnet for en refusjon, som
 * også betyr at eventuelle 5G-grenser gjaldt for den refusjonen også. Ved å trekke fra helt til sist kan vi også fange opp
 * eventuelle avvik der feks refusjonen ble beregnet med feil grunnbeløp (vil dermed få litt mer utbetalt i korreksjonen).
 */
fun tilskuddsberegning(
    inntekter: List<Inntektslinje>,
    tilskuddsgrunnlag: Tilskuddsgrunnlag,
    utbetaltIRefusjonSomSkalKorrigeres: Int,
    manueltJustertBruttolønn: Int? = null,
    fratrekkRefunderbarSum: Int? = null,
    forrigeRefusjonMinusBeløp: Int = 0,
    tilskuddFom: LocalDate,
    sumUtbetaltForTiltaketIÅr: Int = 0,
    harFerietrekkForSammeMåned: Boolean,
    beregningskontekst: Beregningskontekst,
): Beregning {
    val kalkulertBruttoLønn = kalkulerBruttoLønn(inntekter).roundToInt()
    val lønn = if (manueltJustertBruttolønn != null) minOf(
        manueltJustertBruttolønn,
        kalkulertBruttoLønn
    ) else kalkulertBruttoLønn
    val trekkgrunnlagFerie =
        if (harFerietrekkForSammeMåned) 0 else leggSammenTrekkGrunnlag(inntekter, tilskuddFom).roundToInt()
    val lønnFratrukketFerie = lønn + trekkgrunnlagFerie
    val feriepenger = lønnFratrukketFerie * tilskuddsgrunnlag.feriepengerSats
    val tjenestepensjon = (lønnFratrukketFerie + feriepenger) * tilskuddsgrunnlag.otpSats
    val arbeidsgiveravgift =
        (lønnFratrukketFerie + tjenestepensjon + feriepenger) * tilskuddsgrunnlag.arbeidsgiveravgiftSats
    val sumUtgifter = lønnFratrukketFerie + tjenestepensjon + feriepenger + arbeidsgiveravgift
    val fratrekkRefunderbarBeløp = fratrekkRefunderbarSum ?: 0
    val sumUtgifterFratrukketRefundertBeløp = sumUtgifter - fratrekkRefunderbarBeløp
    val beregnetBeløpUtenFratrukketRefundertBeløp = sumUtgifter * (tilskuddsgrunnlag.lønnstilskuddsprosent / 100.0)
    var beregnetBeløp = sumUtgifterFratrukketRefundertBeløp * (tilskuddsgrunnlag.lønnstilskuddsprosent / 100.0)

    if (beregnetBeløpUtenFratrukketRefundertBeløp > 0 && beregnetBeløp < 0) {
        beregnetBeløp = 0.0
    }
    if (beregnetBeløpUtenFratrukketRefundertBeløp < 0) {
        beregnetBeløp = beregnetBeløpUtenFratrukketRefundertBeløp
    }

    val avrundetBeregnetBeløp: Int = beregnetBeløp.roundToInt()

    val overTilskuddsbeløp = avrundetBeregnetBeløp > tilskuddsgrunnlag.tilskuddsbeløp
    var refusjonsbeløp: Int =
        (if (overTilskuddsbeløp) tilskuddsgrunnlag.tilskuddsbeløp else avrundetBeregnetBeløp) + forrigeRefusjonMinusBeløp
    val grunnbelopForPerioden: Grunnbelop = beregningskontekst.grunnbelopForPerioden(tilskuddFom)

    var overFemGrunnbeløp = false
    if (tilskuddsgrunnlag.tiltakstype.har5gBegrensning()) {
        val resultat = beregningskontekst.gjenståendeEtterMaks5G(tilskuddFom, sumUtbetaltForTiltaketIÅr, refusjonsbeløp)

        if (resultat is Maks5GResultat.OverMaks) {
            refusjonsbeløp = resultat.maksbelop
            overFemGrunnbeløp = true
        }
    }

    // Hvis vi korrigerer vil det være et utbetalt beløp på den korrigerte refusjonen som vi må trekke fra til sist
    refusjonsbeløp = refusjonsbeløp - utbetaltIRefusjonSomSkalKorrigeres

    return Beregning(
        lønn = lønn,
        lønnFratrukketFerie = lønnFratrukketFerie,
        feriepenger = feriepenger.roundToInt(),
        tjenestepensjon = tjenestepensjon.roundToInt(),
        arbeidsgiveravgift = arbeidsgiveravgift.roundToInt(),
        sumUtgifter = sumUtgifter.roundToInt(),
        beregnetBeløp = avrundetBeregnetBeløp,
        refusjonsbeløp = refusjonsbeløp,
        overTilskuddsbeløp = overTilskuddsbeløp,
        tidligereUtbetalt = utbetaltIRefusjonSomSkalKorrigeres,
        fratrekkLønnFerie = trekkgrunnlagFerie,
        tidligereRefundertBeløp = fratrekkRefunderbarBeløp,
        overFemGrunnbeløp = overFemGrunnbeløp,
        sumUtgifterFratrukketRefundertBeløp = sumUtgifterFratrukketRefundertBeløp.roundToInt(),
        grunnbelopBrukt = grunnbelopForPerioden.belop,
        grunnbelopDato = grunnbelopForPerioden.gjelderFraOgMed,
    )
}

fun beregn(beregningskontekst: Beregningskontekst, refundering: Refundering): Beregning? {
    if (!refundering.refusjonsgrunnlag.harTilstrekkeligInformasjonForBeregning()) {
        return null
    }

    return when (refundering.tiltakstype()) {
        Tiltakstype.VTAO -> fastBeløpBeregning(
            refundering.refusjonsgrunnlag.tilskuddsgrunnlag,
            refundering.refusjonsgrunnlag.tidligereUtbetalt,
            when (refundering) {
                is Korreksjon -> true; is Refusjon -> false
            }
        )

        Tiltakstype.MENTOR -> when (refundering) {
            is Korreksjon -> null
            is Refusjon -> mentorBeregning(refundering.refusjonsgrunnlag.tilskuddsgrunnlag)
        }

        Tiltakstype.SOMMERJOBB, Tiltakstype.VARIG_LONNSTILSKUDD, Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD, Tiltakstype.FIREARIG_LONNSTILSKUDD -> tilskuddsberegning(
            inntekter = refundering.refusjonsgrunnlag.inntektsgrunnlag?.inntekter?.toList() ?: emptyList(),
            tilskuddsgrunnlag = refundering.refusjonsgrunnlag.tilskuddsgrunnlag,
            utbetaltIRefusjonSomSkalKorrigeres = refundering.refusjonsgrunnlag.tidligereUtbetalt,
            manueltJustertBruttolønn = refundering.refusjonsgrunnlag.endretBruttoLønn,
            fratrekkRefunderbarSum = refundering.refusjonsgrunnlag.refunderbarBeløp,
            forrigeRefusjonMinusBeløp = refundering.refusjonsgrunnlag.forrigeRefusjonMinusBeløp,
            tilskuddFom = refundering.refusjonsgrunnlag.tilskuddsgrunnlag.tilskuddFom,
            sumUtbetaltForTiltaketIÅr = refundering.refusjonsgrunnlag.sumUtbetaltVarig,
            harFerietrekkForSammeMåned = refundering.refusjonsgrunnlag.harFerietrekkForSammeMåned,
            beregningskontekst = beregningskontekst
        )
    }
}


fun leggSammenTrekkGrunnlag(
    inntekter: List<Inntektslinje>,
    tilskuddFom: LocalDate
): Double = inntekter.filter { it.skalTrekkesIfraInntektsgrunnlag(tilskuddFom) }.sumOf { it.beløp }

fun kalkulerBruttoLønn(
    inntekter: List<Inntektslinje>,
): Double =
    inntekter.filter { it.erMedIInntektsgrunnlag() && it.erOpptjentIPeriode != null && it.erOpptjentIPeriode!! }
        .sumOf { it.beløp }
