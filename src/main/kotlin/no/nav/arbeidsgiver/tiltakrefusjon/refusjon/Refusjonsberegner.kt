package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import java.time.LocalDate
import kotlin.math.roundToInt

private fun beløpPerInntektslinje(
    inntektslinje: Inntektslinje,
    tilskuddFom: LocalDate,
    tilskuddTom: LocalDate,
): Double {
    if (inntektslinje.opptjeningsperiodeFom == null || inntektslinje.opptjeningsperiodeTom == null)
        return if (erMånedIPeriode(inntektslinje.måned, tilskuddFom, tilskuddTom)) {
            inntektslinje.beløp
        } else {
            0.0
        }

    if (inntektslinje.opptjeningsperiodeTom < tilskuddFom) {
        return 0.0;
    }

    val antallDagerSkalFordelesPå = antallDager(inntektslinje.opptjeningsperiodeFom, inntektslinje.opptjeningsperiodeTom)
    var dagsats = inntektslinje.beløp / antallDagerSkalFordelesPå


    return dagsats * antallDager(maxOf(tilskuddFom, inntektslinje.opptjeningsperiodeFom), minOf(tilskuddTom, inntektslinje.opptjeningsperiodeTom))
}

private fun antallDager(
    fom: LocalDate,
    tom: LocalDate,
) = fom.datesUntil(tom.plusDays(1)).count().toInt()

fun beregnRefusjonsbeløp(
    inntekter: List<Inntektslinje>,
    tilskuddsgrunnlag: Tilskuddsgrunnlag,
    appImageId: String,
): Beregning {
    val lønn = inntekter
        .filter(Inntektslinje::erMedIInntektsgrunnlag)
        .sumOf { beløpPerInntektslinje(it, tilskuddsgrunnlag.tilskuddFom, tilskuddsgrunnlag.tilskuddTom) }
    val feriepenger = lønn * tilskuddsgrunnlag.feriepengerSats
    val tjenestepensjon = (lønn + feriepenger) * tilskuddsgrunnlag.otpSats
    val arbeidsgiveravgift = (lønn + tjenestepensjon + feriepenger) * tilskuddsgrunnlag.arbeidsgiveravgiftSats
    val sumUtgifter = lønn + tjenestepensjon + feriepenger + arbeidsgiveravgift
    var beregnetBeløp = sumUtgifter * (tilskuddsgrunnlag.lønnstilskuddsprosent / 100.0)

    val overTilskuddsbeløp = beregnetBeløp > tilskuddsgrunnlag.tilskuddsbeløp
    val refusjonsbeløp = if (overTilskuddsbeløp) tilskuddsgrunnlag.tilskuddsbeløp.toDouble() else beregnetBeløp

    return Beregning(
        lønn = lønn.roundToInt(),
        feriepenger = feriepenger.roundToInt(),
        tjenestepensjon = tjenestepensjon.roundToInt(),
        arbeidsgiveravgift = arbeidsgiveravgift.roundToInt(),
        sumUtgifter = sumUtgifter.roundToInt(),
        beregnetBeløp = beregnetBeløp.roundToInt(),
        refusjonsbeløp = refusjonsbeløp.roundToInt(),
        overTilskuddsbeløp = overTilskuddsbeløp,
        appImageId = appImageId
    )
}