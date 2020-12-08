package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import java.time.LocalDate
import kotlin.math.roundToInt
import kotlin.streams.toList

private fun inntektsdager(inntektslinje: Inntektslinje, tilskuddFom: LocalDate, tilskuddTom: LocalDate): List<Double> {
    val inntektFom = inntektslinje.opptjeningsperiodeFom ?: inntektslinje.måned.atDay(1)
    val inntektTom = inntektslinje.opptjeningsperiodeTom ?: inntektslinje.måned.atEndOfMonth()
    val antallDagerOpptjent = inntektFom.datesUntil(inntektTom.plusDays(1)).count().toInt()
    var beløpPerDag = inntektslinje.beløp / antallDagerOpptjent

    val overlappFom = maxOf(inntektFom, tilskuddFom)
    val overlappTom = minOf(inntektTom, tilskuddTom)

    if (overlappFom > overlappTom) {
        return emptyList()
    }

    return overlappFom.datesUntil(overlappTom.plusDays(1)).map { beløpPerDag }.toList()
}

fun beregnRefusjonsbeløp(inntekter: List<Inntektslinje>, tilskuddsgrunnlag: Tilskuddsgrunnlag): Beregning {
    val lønn = inntekter
            .filter(Inntektslinje::erLønnsinntekt)
            .flatMap { inntektsdager(it, tilskuddsgrunnlag.tilskuddFom, tilskuddsgrunnlag.tilskuddTom) }
            .sum()
    val feriepenger = lønn * tilskuddsgrunnlag.feriepengerSats
    val tjenestepensjon = (lønn + feriepenger) * tilskuddsgrunnlag.otpSats
    val arbeidsgiveravgift = (lønn + tjenestepensjon + feriepenger) * tilskuddsgrunnlag.arbeidsgiveravgiftSats
    val sumUtgifter = lønn + tjenestepensjon + feriepenger + arbeidsgiveravgift
    val refusjonsbeløp = sumUtgifter * (tilskuddsgrunnlag.lønnstilskuddsprosent / 100.0)

    return Beregning(
            lønn = lønn.roundToInt(),
            feriepenger = feriepenger.roundToInt(),
            tjenestepensjon = tjenestepensjon.roundToInt(),
            arbeidsgiveravgift = arbeidsgiveravgift.roundToInt(),
            sumUtgifter = sumUtgifter.roundToInt(),
            refusjonsbeløp = refusjonsbeløp.roundToInt(),
    )
}