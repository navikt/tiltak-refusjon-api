package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import java.time.LocalDate
import kotlin.math.roundToInt

data class Refusjonsgrunnlag(
        val inntekter: List<Inntektslinje>,
        val refusjonsgrad: Int,
        val datoRefusjonstart: LocalDate,
        val datoRefusjonslutt: LocalDate,
        var arbeidsgiveravgiftSats: Double,
        var feriepengerSats: Double,
        var tjenestepensjonSats: Double
) {
    fun hentBeregnetGrunnlag(): Int {
        return inntekter
                .filter(Inntektslinje::erLønnsinntekt)
                .map { inntekt ->
                    val dagerOpptjentInnenRefusjonsperiode = inntekt.hentAntallDagerOpptjentInnenPeriode(datoRefusjonstart, datoRefusjonslutt)
                    if( dagerOpptjentInnenRefusjonsperiode == 0 ) return 0

                    val beløpPerDag = inntekt.hentBeløpPerDag()

                    val feriepengerPerDag = beløpPerDag * feriepengerSats
                    val tjenestepensjonPerDag = (beløpPerDag + feriepengerPerDag) * tjenestepensjonSats
                    val arbeidsgiveravgiftPerDag = (beløpPerDag + tjenestepensjonPerDag + feriepengerPerDag) * arbeidsgiveravgiftSats
                    val totalBeløpPerDag =  beløpPerDag + tjenestepensjonPerDag + feriepengerPerDag + arbeidsgiveravgiftPerDag

                    totalBeløpPerDag.times(dagerOpptjentInnenRefusjonsperiode)
                }
                .sum()
                .times(refusjonsgrad / 100.0)
                .roundToInt()
    }
}
