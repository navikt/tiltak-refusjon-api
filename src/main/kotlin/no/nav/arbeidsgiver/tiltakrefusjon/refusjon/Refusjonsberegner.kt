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

fun beregnRefusjonsbeløp(inntekter: List<Inntektslinje>, tilskuddsgrunnlag: Tilskuddsgrunnlag): Int {
    val totalRefundertForHelePeriode = inntekter
            .filter(Inntektslinje::erLønnsinntekt)
            .flatMap { inntektsdager(it, tilskuddsgrunnlag.tilskuddFom, tilskuddsgrunnlag.tilskuddTom) }
            .map { beløpPerDag ->
                val feriepengerPerDag = beløpPerDag * tilskuddsgrunnlag.feriepengerSats
                val tjenestepensjonPerDag = (beløpPerDag + feriepengerPerDag) * tilskuddsgrunnlag.otpSats
                val arbeidsgiveravgiftPerDag = (beløpPerDag + tjenestepensjonPerDag + feriepengerPerDag) * tilskuddsgrunnlag.arbeidsgiveravgiftSats
                val totaltRefundertPerDag = beløpPerDag + tjenestepensjonPerDag + feriepengerPerDag + arbeidsgiveravgiftPerDag

                totaltRefundertPerDag
            }
            .sum() * tilskuddsgrunnlag.lønnstilskuddsprosent.toDouble() / 100.0
    return totalRefundertForHelePeriode.roundToInt()
}