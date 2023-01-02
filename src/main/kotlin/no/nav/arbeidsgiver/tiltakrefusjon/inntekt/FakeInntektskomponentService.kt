package no.nav.arbeidsgiver.tiltakrefusjon.inntekt

import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.Inntektslinje
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.Period
import java.time.YearMonth
import kotlin.streams.toList

@Service
@ConditionalOnProperty("tiltak-refusjon.inntektskomponenten.fake")
class FakeInntektskomponentService : InntektskomponentService {
    override fun hentInntekter(
        fnr: String,
        bedriftnummerDetSøkesPå: String,
        datoFra: LocalDate,
        datoTil: LocalDate
    ): Pair<List<Inntektslinje>, String> {
        if (fnr == "07098142678") {
            return Pair(emptyList(), "")
        }else if (fnr == "08098613316"){
            // Simulerer minus beløp for (Jon Janson Minus Beløp) i test data
            val inntektslinjer = ArrayList<Inntektslinje>()
            val måned = YearMonth.of(datoFra.year, datoFra.month)
            inntektslinjer.add(Inntektslinje("LOENNSINNTEKT", "fastloenn", 20000.0,  måned, datoTil, måned.atEndOfMonth()))
            inntektslinjer.add(Inntektslinje("LOENNSINNTEKT", "trekkILoennForFerie", -25000.0,  måned, datoTil, måned.atEndOfMonth()))
            return Pair(inntektslinjer, "fake respons")
        }

        val inntektslinjer = ArrayList<Inntektslinje>()
        datoFra.datesUntil(datoTil, Period.ofMonths(1)).forEach {
            val måned = YearMonth.of(it.year, it.month)
//            inntektslinjer.add(Inntektslinje("LOENNSINNTEKT", "timeloenn", 250000.0, måned, it, måned.atEndOfMonth()))
            //inntektslinjer.add(Inntektslinje("LOENNSINNTEKT", "fastloenn", 10000.0, måned, it, måned.atEndOfMonth()))
            inntektslinjer.add(Inntektslinje("LOENNSINNTEKT", "fastloenn", 10000.0, måned, it, måned.atEndOfMonth()))
            if (Math.random() > 0.5) {
                inntektslinjer.add(Inntektslinje("LOENNSINNTEKT", "fastloenn", 2000.0, måned, it, måned.atEndOfMonth()))
            }
            inntektslinjer.add(Inntektslinje("LOENNSINNTEKT", "trekkILoennForFerie", -1200.0, måned, it, måned.atEndOfMonth()))
//            inntektslinjer.add(Inntektslinje("LOENNSINNTEKT", "fastTillegg", 10000.0, måned, null, null))
//            inntektslinjer.add(Inntektslinje("LOENNSINNTEKT", "overtidsgodtgjoerelse", 7683.0, måned, it, måned.atEndOfMonth()))
            inntektslinjer.add(Inntektslinje("LOENNSINNTEKT", "loennUtbetaltAvVeldedigEllerAllmennyttigInstitusjonEllerOrganisasjon", 423.0, måned, it, måned.atEndOfMonth(), erOpptjentIPeriode = false))
        }
        return Pair(inntektslinjer, "fake respons")
    }
}

