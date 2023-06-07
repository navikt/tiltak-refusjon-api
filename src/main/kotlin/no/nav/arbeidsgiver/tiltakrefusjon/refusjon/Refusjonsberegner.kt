package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import no.nav.arbeidsgiver.tiltakrefusjon.utils.erMånedIPeriode
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

fun beregnRefusjonsbeløp(
    inntekter: List<Inntektslinje>,
    tilskuddsgrunnlag: Tilskuddsgrunnlag,
    tidligereUtbetalt: Int,
    korrigertBruttoLønn: Int? = null,
    fratrekkRefunderbarSum: Int? = null,
    forrigeRefusjonMinusBeløp: Int = 0,
    tilskuddFom: LocalDate
): Beregning {
    val kalkulertBruttoLønn = kalkulerBruttoLønn(inntekter).roundToInt()
    val lønn = if (korrigertBruttoLønn != null) minOf(korrigertBruttoLønn, kalkulertBruttoLønn) else kalkulertBruttoLønn
    val trekkgrunnlagFerie = leggSammenTrekkGrunnlag(inntekter, tilskuddFom).roundToInt()
    val fratrekkRefunderbarBeløp = fratrekkRefunderbarSum ?: 0
    val lønnFratrukketFerie = lønn - trekkgrunnlagFerie
    val feriepenger = lønnFratrukketFerie * tilskuddsgrunnlag.feriepengerSats
    val tjenestepensjon = (lønnFratrukketFerie + feriepenger) * tilskuddsgrunnlag.otpSats
    val arbeidsgiveravgift = (lønnFratrukketFerie + tjenestepensjon + feriepenger) * tilskuddsgrunnlag.arbeidsgiveravgiftSats
    val sumUtgifter = lønnFratrukketFerie + tjenestepensjon + feriepenger + arbeidsgiveravgift
    val sumUtgifterFratrukketRefundertBeløp = sumUtgifter - fratrekkRefunderbarBeløp
    val beregnetBeløpUtenFratrukketRefundertBeløp = sumUtgifter  * (tilskuddsgrunnlag.lønnstilskuddsprosent / 100.0)
    var beregnetBeløp = sumUtgifterFratrukketRefundertBeløp * (tilskuddsgrunnlag.lønnstilskuddsprosent / 100.0)

    if (beregnetBeløpUtenFratrukketRefundertBeløp > 0 && beregnetBeløp < 0) {
        beregnetBeløp = 0.0
    }
    if (beregnetBeløpUtenFratrukketRefundertBeløp < 0) {
        beregnetBeløp = beregnetBeløpUtenFratrukketRefundertBeløp
    }

    val overTilskuddsbeløp = beregnetBeløp > tilskuddsgrunnlag.tilskuddsbeløp
    var refusjonsbeløp =
        (if (overTilskuddsbeløp) tilskuddsgrunnlag.tilskuddsbeløp.toDouble() else beregnetBeløp) - (if(tidligereUtbetalt < 0) tidligereUtbetalt  * -1 else tidligereUtbetalt) + forrigeRefusjonMinusBeløp

    return Beregning(
        lønn = lønn,
        lønnFratrukketFerie = lønnFratrukketFerie,
        feriepenger = feriepenger.roundToInt(),
        tjenestepensjon = tjenestepensjon.roundToInt(),
        arbeidsgiveravgift = arbeidsgiveravgift.roundToInt(),
        sumUtgifter = sumUtgifter.roundToInt(),
        beregnetBeløp = beregnetBeløp.roundToInt(),
        refusjonsbeløp = refusjonsbeløp.roundToInt(),
        overTilskuddsbeløp = overTilskuddsbeløp,
        tidligereUtbetalt = tidligereUtbetalt,
        fratrekkLønnFerie = trekkgrunnlagFerie,
        tidligereRefundertBeløp = fratrekkRefunderbarBeløp,
        sumUtgifterFratrukketRefundertBeløp = sumUtgifterFratrukketRefundertBeløp.roundToInt())
}

fun leggSammenTrekkGrunnlag(
    inntekter: List<Inntektslinje>,
    tilskuddFom: LocalDate
): Double =
    inntekter.filter { it.skalTrekkesIfraInntektsgrunnlag(tilskuddFom) }
        .sumOf { inntekt -> if (inntekt.beløp < 0) (inntekt.beløp * -1) else inntekt.beløp }

fun kalkulerBruttoLønn(
    inntekter: List<Inntektslinje>,
): Double =
    inntekter.filter { it.erMedIInntektsgrunnlag() && it.erOpptjentIPeriode != null && it.erOpptjentIPeriode!! }
        .sumOf { it.beløp }