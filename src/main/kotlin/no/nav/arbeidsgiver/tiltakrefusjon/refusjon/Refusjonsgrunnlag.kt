package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import java.time.LocalDate
import kotlin.math.roundToInt

data class Refusjonsgrunnlag(
        val inntekter: List<Inntektslinje>,
        val refusjonsgrad: Int,
        val datoRefusjonstart: LocalDate,
        val datoRefusjonslutt: LocalDate,
        var arbeidsgiveravgiftSats: Double?,
        var feriepengerSats: Double?,
        var tjenestepensjonSats: Double?
) {
    fun hentBeregnetGrunnlag(): Int {
        return inntekter
                .filter(Inntektslinje::erLønnsinntekt)
                .map { inntekt ->
                    val dagerOpptjentInnenRefusjonsperiode = inntekt.hentAntallOpptjenteDagerInnenPeriode(datoRefusjonstart, datoRefusjonslutt)
                    if( dagerOpptjentInnenRefusjonsperiode == 0 ) return 0
                    val beløpPerDag = inntekt.hentBeløpPerDag()
                    val feriepenger = beløpPerDag * feriepengerSats!!
                    val tjenestepensjon = (beløpPerDag + feriepenger) * tjenestepensjonSats!!
                    val arbeidsgiveravgift = (beløpPerDag + tjenestepensjon + feriepenger) * arbeidsgiveravgiftSats!!
                    val total =  beløpPerDag + tjenestepensjon + feriepenger + arbeidsgiveravgift
                    total.times(dagerOpptjentInnenRefusjonsperiode)
                }
                .sum()
                .times(refusjonsgrad / 100.0)
                .roundToInt()
    }

    fun hentAntallDagerForGittInntekt(){

    }
}
