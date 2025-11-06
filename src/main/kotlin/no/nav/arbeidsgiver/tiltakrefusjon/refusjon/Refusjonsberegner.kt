package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import no.nav.arbeidsgiver.tiltakrefusjon.utils.erMånedIPeriode
import no.nav.arbeidsgiver.tiltakrefusjon.utils.gjenståendeEtterMaks5G
import java.time.LocalDate
import kotlin.math.roundToInt

private fun beløpPerInntektslinje(
    inntektslinje: Inntektslinje,
    fom: LocalDate,
    tom: LocalDate,
    tiltakstype: Tiltakstype,
): Double {
    if (inntektslinje.opptjeningsperiodeFom == null || inntektslinje.opptjeningsperiodeTom == null || tiltakstype == Tiltakstype.SOMMERJOBB)
        return if (erMånedIPeriode(inntektslinje.måned, fom, tom)) {
            inntektslinje.beløp
        } else {
            0.0
        }

    if (inntektslinje.opptjeningsperiodeTom < fom) {
        return 0.0
    }

    val antallDagerSkalFordelesPå =
        antallDager(inntektslinje.opptjeningsperiodeFom, inntektslinje.opptjeningsperiodeTom)
    val dagsats = inntektslinje.beløp / antallDagerSkalFordelesPå


    return dagsats * antallDager(
        maxOf(fom, inntektslinje.opptjeningsperiodeFom),
        minOf(tom, inntektslinje.opptjeningsperiodeTom)
    )
}

private fun antallDager(
    fom: LocalDate,
    tom: LocalDate,
) = fom.datesUntil(tom.plusDays(1)).count().toInt()

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

fun mentorBeregning(tilskuddsgrunnlag: Tilskuddsgrunnlag): Beregning? {
    if (tilskuddsgrunnlag.mentorTimelonn == null || tilskuddsgrunnlag.mentorAntallTimer == null) {
        return null
    }
    val lonn = tilskuddsgrunnlag.mentorAntallTimer * tilskuddsgrunnlag.mentorTimelonn
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
    var overFemGrunnbeløp = false
    if (tilskuddsgrunnlag.tiltakstype == Tiltakstype.VARIG_LONNSTILSKUDD) {
        if (refusjonsbeløp > gjenståendeEtterMaks5G(sumUtbetaltVarig, tilskuddFom)) {
            refusjonsbeløp = gjenståendeEtterMaks5G(sumUtbetaltVarig, tilskuddFom)
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
        sumUtgifterFratrukketRefundertBeløp = sumUtgifterFratrukketRefundertBeløp.roundToInt()
    )
}

fun beregnRefusjon(refusjon: Refusjon) =
    if (refusjon.tiltakstype().harFastUtbetalingssum()) {
        if (refusjon.tiltakstype() == Tiltakstype.VTAO) {
            fastBeløpBeregning(refusjon.refusjonsgrunnlag.tilskuddsgrunnlag, refusjon.refusjonsgrunnlag.tidligereUtbetalt)
        } else if (refusjon.tiltakstype() == Tiltakstype.MENTOR) {
            mentorBeregning(refusjon.refusjonsgrunnlag.tilskuddsgrunnlag)
        } else {
            throw Exception("Ukjent tiltakstype med fast sum: ${refusjon.tiltakstype()}")
        }
    } else if (refusjon.refusjonsgrunnlag.harTilstrekkeligInformasjonForBeregning()) {
        beregnRefusjonsbeløp(
            inntekter = refusjon.refusjonsgrunnlag.inntektsgrunnlag?.inntekter?.toList() ?: emptyList(),
            tilskuddsgrunnlag = refusjon.refusjonsgrunnlag.tilskuddsgrunnlag,
            tidligereUtbetalt = refusjon.refusjonsgrunnlag.tidligereUtbetalt,
            korrigertBruttoLønn = refusjon.refusjonsgrunnlag.endretBruttoLønn,
            fratrekkRefunderbarSum = refusjon.refusjonsgrunnlag.refunderbarBeløp,
            forrigeRefusjonMinusBeløp = refusjon.refusjonsgrunnlag.forrigeRefusjonMinusBeløp,
            tilskuddFom = refusjon.refusjonsgrunnlag.tilskuddsgrunnlag.tilskuddFom,
            sumUtbetaltVarig = refusjon.refusjonsgrunnlag.sumUtbetaltVarig,
            harFerietrekkForSammeMåned = refusjon.refusjonsgrunnlag.harFerietrekkForSammeMåned
        )
    } else {
        null
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
