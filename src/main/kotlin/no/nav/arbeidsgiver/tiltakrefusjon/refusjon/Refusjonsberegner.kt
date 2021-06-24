package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import java.time.LocalDate
import kotlin.math.roundToInt
import kotlin.streams.toList

private fun inntektsdager(inntektslinje: Inntektslinje, tilskuddFom: LocalDate, tilskuddTom: LocalDate): List<Double> {
    val antallDagerOpptjent =
        inntektslinje.inntektFordelesFom().datesUntil(inntektslinje.inntektFordelesTom().plusDays(1)).count().toInt()
    val beløpPerDag = inntektslinje.beløp / antallDagerOpptjent

    val overlappFom = maxOf(inntektslinje.inntektFordelesFom(), tilskuddFom)
    val overlappTom = minOf(inntektslinje.inntektFordelesTom(), tilskuddTom)

    if (overlappFom > overlappTom) {
        return emptyList()
    }

    return overlappFom.datesUntil(overlappTom.plusDays(1)).map { beløpPerDag }.toList()
}

fun beregnRefusjonsbeløp(inntekter: List<Inntektslinje>, tilskuddsgrunnlag: Tilskuddsgrunnlag, commitHash: String): Beregning {
    val lønn = inntekter
        .filter(Inntektslinje::erMedIInntektsgrunnlag)
        .flatMap { inntektsdager(it, tilskuddsgrunnlag.tilskuddFom, tilskuddsgrunnlag.tilskuddTom) }
        .sum()
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
        commitHash = commitHash
    )
}