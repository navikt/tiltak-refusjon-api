package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import java.time.LocalDate
import kotlin.math.roundToInt

fun fastBeløpBeregning(tilskuddsgrunnlag: Tilskuddsgrunnlag, tidligereUtbetalt: Int, korriger: Boolean = false): Beregning {
    val beregnetBeløp = if (korriger) 0 else tilskuddsgrunnlag.tilskuddsbeløp

    return Beregning(
        lønn = 0,
        lønnFratrukketFerie = 0,
        feriepenger = 0,
        tjenestepensjon = 0,
        arbeidsgiveravgift = 0,
        sumUtgifter = 0,
        beregnetBeløp = beregnetBeløp,
        refusjonsbeløp = beregnetBeløp - tidligereUtbetalt,
        overTilskuddsbeløp = false,
        tidligereUtbetalt = tidligereUtbetalt,
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

fun beregnRefusjonsbeløp(
    inntekter: List<Inntektslinje>,
    tilskuddsgrunnlag: Tilskuddsgrunnlag,
    tidligereUtbetalt: Int,
    korrigertBruttoLønn: Int? = null,
    fratrekkRefunderbarSum: Int? = null,
    forrigeRefusjonMinusBeløp: Int = 0,
    tilskuddFom: LocalDate,
    sumUtbetaltVarig: Int = 0,
    harFerietrekkForSammeMåned: Boolean,
    ekstraFerietrekk: Int? = null,
    beregningskontekst: Beregningskontekst,

    ): Beregning {
    val kalkulertBruttoLønn = kalkulerBruttoLønn(inntekter).roundToInt()
    val lønn = if (korrigertBruttoLønn != null) minOf(korrigertBruttoLønn, kalkulertBruttoLønn) else kalkulertBruttoLønn
    val trekkgrunnlagFerie = if (harFerietrekkForSammeMåned) 0 else leggSammenTrekkGrunnlag(inntekter, tilskuddFom, ekstraFerietrekk).roundToInt()
    val fratrekkRefunderbarBeløp = fratrekkRefunderbarSum ?: 0
    val lønnFratrukketFerie = lønn + trekkgrunnlagFerie
    val feriepenger = lønnFratrukketFerie * tilskuddsgrunnlag.feriepengerSats
    val tjenestepensjon = (lønnFratrukketFerie + feriepenger) * tilskuddsgrunnlag.otpSats
    val arbeidsgiveravgift = (lønnFratrukketFerie + tjenestepensjon + feriepenger) * tilskuddsgrunnlag.arbeidsgiveravgiftSats
    val sumUtgifter = lønnFratrukketFerie + tjenestepensjon + feriepenger + arbeidsgiveravgift
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
        (if (overTilskuddsbeløp) tilskuddsgrunnlag.tilskuddsbeløp else avrundetBeregnetBeløp) - tidligereUtbetalt + forrigeRefusjonMinusBeløp
    val grunnbelopForPerioden: Map.Entry<LocalDate, Int> = beregningskontekst.alleGrunnbelop.floorEntry(tilskuddFom)
        // Antas å kun inntreffe dersom vi ikke har et grunnbeløp for en periode veldig langt tilbake i tid,
        // feks før 1970, eller dersom api-kall returnerte tom liste
        ?: throw RuntimeException("Fant ikke grunnbeløp for periode $tilskuddFom")

    var overFemGrunnbeløp = false
    if (tilskuddsgrunnlag.tiltakstype.kanIkkeOverskride5g()) {
        val maksBelopForPerioden = gjenståendeEtterMaks5G(grunnbelopForPerioden.value, sumUtbetaltVarig)

        if (refusjonsbeløp > maksBelopForPerioden) {
            refusjonsbeløp = maksBelopForPerioden
            overFemGrunnbeløp = true
        }
    }

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
        tidligereUtbetalt = tidligereUtbetalt,
        fratrekkLønnFerie = trekkgrunnlagFerie,
        tidligereRefundertBeløp = fratrekkRefunderbarBeløp,
        overFemGrunnbeløp = overFemGrunnbeløp,
        sumUtgifterFratrukketRefundertBeløp = sumUtgifterFratrukketRefundertBeløp.roundToInt(),
        grunnbelopBrukt = grunnbelopForPerioden?.value,
        grunnbelopDato = grunnbelopForPerioden?.key,
    )
}

// Returnerer det man får opp til 5G. Altså 5G - Totalt utbetalt
private fun gjenståendeEtterMaks5G(grunnbelop: Int, sumUtbetalt: Int): Int {
    return 0.coerceAtLeast(5 * grunnbelop - sumUtbetalt)
}

fun beregnRefusjon(beregningskontekst: Beregningskontekst, refusjon: Refusjon): Beregning? {
    if (!refusjon.refusjonsgrunnlag.harTilstrekkeligInformasjonForBeregning()) {
        return null
    }

    return when (refusjon.tiltakstype()) {
        Tiltakstype.VTAO -> fastBeløpBeregning(refusjon.refusjonsgrunnlag.tilskuddsgrunnlag, refusjon.refusjonsgrunnlag.tidligereUtbetalt)
        Tiltakstype.MENTOR -> mentorBeregning(refusjon.refusjonsgrunnlag.tilskuddsgrunnlag)
        Tiltakstype.SOMMERJOBB, Tiltakstype.VARIG_LONNSTILSKUDD, Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD, Tiltakstype.FIREARIG_LONNSTILSKUDD -> beregnRefusjonsbeløp(
            inntekter = refusjon.refusjonsgrunnlag.inntektsgrunnlag?.inntekter?.toList() ?: emptyList(),
            tilskuddsgrunnlag = refusjon.refusjonsgrunnlag.tilskuddsgrunnlag,
            tidligereUtbetalt = refusjon.refusjonsgrunnlag.tidligereUtbetalt,
            korrigertBruttoLønn = refusjon.refusjonsgrunnlag.endretBruttoLønn,
            fratrekkRefunderbarSum = refusjon.refusjonsgrunnlag.refunderbarBeløp,
            forrigeRefusjonMinusBeløp = refusjon.refusjonsgrunnlag.forrigeRefusjonMinusBeløp,
            tilskuddFom = refusjon.refusjonsgrunnlag.tilskuddsgrunnlag.tilskuddFom,
            sumUtbetaltVarig = refusjon.refusjonsgrunnlag.sumUtbetaltVarig,
            harFerietrekkForSammeMåned = refusjon.refusjonsgrunnlag.harFerietrekkForSammeMåned,
            beregningskontekst = beregningskontekst,
        )
    }
}

fun beregnKorreksjon(beregningskontekst: Beregningskontekst, korreksjon: Korreksjon): Beregning? {
    if (!korreksjon.refusjonsgrunnlag.harTilstrekkeligInformasjonForBeregning()) {
        return null
    }

    return when (korreksjon.tiltakstype()) {
        Tiltakstype.VTAO -> fastBeløpBeregning(korreksjon.refusjonsgrunnlag.tilskuddsgrunnlag, korreksjon.refusjonsgrunnlag.tidligereUtbetalt, true)
        Tiltakstype.MENTOR -> null
        Tiltakstype.SOMMERJOBB, Tiltakstype.VARIG_LONNSTILSKUDD, Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD, Tiltakstype.FIREARIG_LONNSTILSKUDD -> beregnRefusjonsbeløp(
            inntekter = korreksjon.refusjonsgrunnlag.inntektsgrunnlag!!.inntekter.toList(),
            tilskuddsgrunnlag = korreksjon.refusjonsgrunnlag.tilskuddsgrunnlag,
            tidligereUtbetalt = korreksjon.refusjonsgrunnlag.tidligereUtbetalt,
            korrigertBruttoLønn = korreksjon.refusjonsgrunnlag.endretBruttoLønn,
            fratrekkRefunderbarSum = korreksjon.refusjonsgrunnlag.refunderbarBeløp,
            forrigeRefusjonMinusBeløp = korreksjon.refusjonsgrunnlag.forrigeRefusjonMinusBeløp,
            tilskuddFom = korreksjon.refusjonsgrunnlag.tilskuddsgrunnlag.tilskuddFom,
            sumUtbetaltVarig = korreksjon.refusjonsgrunnlag.sumUtbetaltVarig,
            harFerietrekkForSammeMåned = korreksjon.refusjonsgrunnlag.harFerietrekkForSammeMåned,
            beregningskontekst = beregningskontekst
        )
    }
}


fun leggSammenTrekkGrunnlag(inntekter: List<Inntektslinje>, tilskuddFom: LocalDate, ekstraFerietrekk: Int? = null): Double {
    var ferieTrekkGrunnlag = inntekter.filter { it.skalTrekkesIfraInntektsgrunnlag(tilskuddFom) }
        .sumOf { it.beløp }
    if (ekstraFerietrekk != null) {
        ferieTrekkGrunnlag += ekstraFerietrekk
    }
    return ferieTrekkGrunnlag
}

fun kalkulerBruttoLønn(
    inntekter: List<Inntektslinje>,
): Double =
    inntekter.filter { it.erMedIInntektsgrunnlag() && it.erOpptjentIPeriode != null && it.erOpptjentIPeriode!! }
        .sumOf { it.beløp }
