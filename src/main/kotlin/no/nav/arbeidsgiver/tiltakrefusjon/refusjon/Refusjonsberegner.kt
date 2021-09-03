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
        return 0.0;
    }

    val antallDagerSkalFordelesPå =
        antallDager(inntektslinje.opptjeningsperiodeFom, inntektslinje.opptjeningsperiodeTom)
    var dagsats = inntektslinje.beløp / antallDagerSkalFordelesPå


    return dagsats * antallDager(maxOf(fom, inntektslinje.opptjeningsperiodeFom),
        minOf(tom, inntektslinje.opptjeningsperiodeTom))
}

private fun antallDager(
    fom: LocalDate,
    tom: LocalDate,
) = fom.datesUntil(tom.plusDays(1)).count().toInt()

fun beregnRefusjonsbeløp(
    inntekter: List<Inntektslinje>,
    tilskuddsgrunnlag: Tilskuddsgrunnlag,
    appImageId: String,
    tidligereUtbetalt: Int,
    korrigertBruttoLønn: Int?,
): Beregning {
    val lønn = korrigertBruttoLønn ?: kalkulerBruttoLønn(inntekter).roundToInt()
    val feriepenger = lønn * tilskuddsgrunnlag.feriepengerSats
    val tjenestepensjon = (lønn + feriepenger) * tilskuddsgrunnlag.otpSats
    val arbeidsgiveravgift = (lønn + tjenestepensjon + feriepenger) * tilskuddsgrunnlag.arbeidsgiveravgiftSats
    val sumUtgifter = lønn + tjenestepensjon + feriepenger + arbeidsgiveravgift
    var beregnetBeløp = sumUtgifter * (tilskuddsgrunnlag.lønnstilskuddsprosent / 100.0)

    val overTilskuddsbeløp = beregnetBeløp > tilskuddsgrunnlag.tilskuddsbeløp
    val refusjonsbeløp =
        (if (overTilskuddsbeløp) tilskuddsgrunnlag.tilskuddsbeløp.toDouble() else beregnetBeløp) - tidligereUtbetalt

    return Beregning(
        lønn = lønn,
        feriepenger = feriepenger.roundToInt(),
        tjenestepensjon = tjenestepensjon.roundToInt(),
        arbeidsgiveravgift = arbeidsgiveravgift.roundToInt(),
        sumUtgifter = sumUtgifter.roundToInt(),
        beregnetBeløp = beregnetBeløp.roundToInt(),
        refusjonsbeløp = refusjonsbeløp.roundToInt(),
        overTilskuddsbeløp = overTilskuddsbeløp,
        tidligereUtbetalt = tidligereUtbetalt,
        appImageId = appImageId
    )
}

fun kalkulerBruttoLønn(
    inntekter: List<Inntektslinje>,
): Double =
    inntekter.filter { it.erMedIInntektsgrunnlag() }.sumOf { it.beløp }
