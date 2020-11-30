package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import java.time.LocalDate
import kotlin.math.roundToInt

data class Refusjonsgrunnlag(
        val inntekter: List<Inntektslinje>,
        val datoRefusjonstart: LocalDate,
        val datoRefusjonslutt: LocalDate,
        val arbeidsgiveravgiftSats: Double,
        val feriepengeSats: Double,
        val otpSats: Double,
        var beløp: Int? = 0
) {

    constructor(inntekter: List<Inntektslinje>, refusjon: Refusjon) : this(
            inntekter,
            refusjon.fraDato,
            refusjon.tilDato,
            refusjon.arbeidsgiveravgiftSats,
            refusjon.feriepengerSats,
            refusjon.otpSats
    )

    init {
        beløp = hentBeregnetGrunnlag()
    }

    private fun hentBeregnetGrunnlag(): Int {
        return inntekter
                .filter(Inntektslinje::erLønnsinntekt)
                .map { it.tilDagsatsForPeriode(datoRefusjonstart, datoRefusjonslutt) }
                .map { dagsats ->
                    val beløpPerDag = dagsats.beløp
                    val feriepengerPerDag = beløpPerDag * feriepengeSats
                    val tjenestepensjonPerDag = (beløpPerDag + feriepengerPerDag) * otpSats
                    val arbeidsgiveravgiftPerDag = (beløpPerDag + tjenestepensjonPerDag + feriepengerPerDag) * arbeidsgiveravgiftSats
                    val totalBeløpPerDag = beløpPerDag + tjenestepensjonPerDag + feriepengerPerDag + arbeidsgiveravgiftPerDag

                    totalBeløpPerDag.times(dagsats.dager)
                }
                .sum()
                .roundToInt()
    }
}
