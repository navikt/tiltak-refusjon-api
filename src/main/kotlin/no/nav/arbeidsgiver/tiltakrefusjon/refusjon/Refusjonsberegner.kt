package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import no.nav.arbeidsgiver.tiltakrefusjon.grunnbelop.Grunnbelop
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.LocalDate
import kotlin.math.roundToInt

val logger: Logger = LoggerFactory.getLogger(RefusjonService::class.java)

fun fastBeløpBeregning(
    tilskuddsgrunnlag: Tilskuddsgrunnlag,
    tidligereUtbetalt: Int,
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
    val trekkgrunnlagFerie = if (harFerietrekkForSammeMåned) 0 else leggSammenTrekkGrunnlag(
        inntekter,
        tilskuddFom,
        ekstraFerietrekk
    ).roundToInt()
    val fratrekkRefunderbarBeløp = fratrekkRefunderbarSum ?: 0
    val lønnFratrukketFerie = lønn + trekkgrunnlagFerie
    val feriepenger = lønnFratrukketFerie * tilskuddsgrunnlag.feriepengerSats
    val tjenestepensjon = (lønnFratrukketFerie + feriepenger) * tilskuddsgrunnlag.otpSats
    val arbeidsgiveravgift =
        (lønnFratrukketFerie + tjenestepensjon + feriepenger) * tilskuddsgrunnlag.arbeidsgiveravgiftSats
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
        (if (overTilskuddsbeløp) tilskuddsgrunnlag.tilskuddsbeløp else avrundetBeregnetBeløp) + forrigeRefusjonMinusBeløp
    val grunnbelopForPerioden: Grunnbelop = beregningskontekst.grunnbelopForPerioden(tilskuddFom)

    var overFemGrunnbeløp = false
    if (tilskuddsgrunnlag.tiltakstype.har5gBegrensning()) {
        val resultat = beregningskontekst.gjenståendeEtterMaks5G(tilskuddFom, sumUtbetaltVarig, refusjonsbeløp)
        val nyttResultat = beregningskontekst.gjenståendeEtterMaks5GNy(tilskuddFom, refusjonsbeløp)

        if (resultat != nyttResultat) {
            logger.warn(
                "Obs! Ny 5g-logikk ville gitt annet resultat. Gammel: {}, ny: {}. Har korreksjoner? {}",
                resultat,
                nyttResultat,
                if (beregningskontekst.innsendteRefunderinger.any { it is Korreksjon }) "ja" else "nei"
            )
        }

        if (resultat is Maks5GResultat.OverMaks) {
            refusjonsbeløp = resultat.maksbelop
            overFemGrunnbeløp = true
        }
    }

    // Hvis vi korrigerer vil det være et utbetalt beløp på den korrigerte refusjonen som vi må trekke fra til sist
    refusjonsbeløp = refusjonsbeløp - tidligereUtbetalt

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
        grunnbelopBrukt = grunnbelopForPerioden.belop,
        grunnbelopDato = grunnbelopForPerioden.gjelderFraOgMed,
    )
}

fun beregnRefusjon(beregningskontekst: Beregningskontekst, refusjon: Refusjon) =
    beregn(beregningskontekst, refusjon)

fun beregnKorreksjon(beregningskontekst: Beregningskontekst, korreksjon: Korreksjon) =
    beregn(beregningskontekst, korreksjon)

private fun beregn(beregningskontekst: Beregningskontekst, refundering: Refundering): Beregning? {
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

        Tiltakstype.SOMMERJOBB, Tiltakstype.VARIG_LONNSTILSKUDD, Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD, Tiltakstype.FIREARIG_LONNSTILSKUDD -> beregnRefusjonsbeløp(
            inntekter = refundering.refusjonsgrunnlag.inntektsgrunnlag?.inntekter?.toList() ?: emptyList(),
            tilskuddsgrunnlag = refundering.refusjonsgrunnlag.tilskuddsgrunnlag,
            tidligereUtbetalt = refundering.refusjonsgrunnlag.tidligereUtbetalt,
            korrigertBruttoLønn = refundering.refusjonsgrunnlag.endretBruttoLønn,
            fratrekkRefunderbarSum = refundering.refusjonsgrunnlag.refunderbarBeløp,
            forrigeRefusjonMinusBeløp = refundering.refusjonsgrunnlag.forrigeRefusjonMinusBeløp,
            tilskuddFom = refundering.refusjonsgrunnlag.tilskuddsgrunnlag.tilskuddFom,
            sumUtbetaltVarig = refundering.refusjonsgrunnlag.sumUtbetaltVarig,
            harFerietrekkForSammeMåned = refundering.refusjonsgrunnlag.harFerietrekkForSammeMåned,
            beregningskontekst = beregningskontekst
        )
    }
}


fun leggSammenTrekkGrunnlag(
    inntekter: List<Inntektslinje>,
    tilskuddFom: LocalDate,
    ekstraFerietrekk: Int? = null
): Double {
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
