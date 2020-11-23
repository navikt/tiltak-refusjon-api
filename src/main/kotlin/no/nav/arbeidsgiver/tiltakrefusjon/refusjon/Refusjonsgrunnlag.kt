package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import java.time.LocalDate
import kotlin.math.roundToInt

data class Refusjonsgrunnlag(
        val inntekter: List<Inntektslinje>,
        val stillingsprosent: Int,
        val datoRefusjonstart: LocalDate,
        val datoRefusjonslutt: LocalDate,
        val arbeidsgiveravgiftSats: Double,
        val feriepengerSats: Double,
        var beløp: Int? = 0
) {
    private val TJENESTEPENSJON_SATS = 0.02

    constructor(inntekter: List<Inntektslinje>, refusjon: Refusjon) : this(inntekter, refusjon.stillingsprosent, refusjon.fraDato, refusjon.tilDato, refusjon.satsArbeidsgiveravgift, refusjon.satsFeriepenger,0)

    init {
        beløp = hentBeregnetGrunnlag()
    }

    fun getBeløp():Int{
        return hentBeregnetGrunnlag()
    }

    private fun hentBeregnetGrunnlag(): Int {
        return inntekter
                .filter(Inntektslinje::erLønnsinntekt)
                .map { it.tilDagsatsForPeriode(datoRefusjonstart, datoRefusjonslutt) }
                .map { dagsats ->
                    val beløpPerDag = dagsats.beløp
                    val feriepengerPerDag = beløpPerDag * feriepengerSats
                    val tjenestepensjonPerDag = (beløpPerDag + feriepengerPerDag) * TJENESTEPENSJON_SATS
                    val arbeidsgiveravgiftPerDag = (beløpPerDag + tjenestepensjonPerDag + feriepengerPerDag) * arbeidsgiveravgiftSats
                    val totalBeløpPerDag = beløpPerDag + tjenestepensjonPerDag + feriepengerPerDag + arbeidsgiveravgiftPerDag

                    totalBeløpPerDag.times(dagsats.dager)
                }
                .sum()
                .times(stillingsprosent / 100.0)
                .roundToInt()
    }
}
