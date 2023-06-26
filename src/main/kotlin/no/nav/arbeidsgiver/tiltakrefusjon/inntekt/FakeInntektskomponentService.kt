package no.nav.arbeidsgiver.tiltakrefusjon.inntekt

import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.Inntektslinje
import no.nav.arbeidsgiver.tiltakrefusjon.utils.Now
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.Period
import java.time.YearMonth
import java.time.temporal.TemporalAdjusters
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
            // Kan ikke ha minus på alle. Må kunne teste positivt. Så kun minus på en inntekt to måneder bak
            if(Period.between(datoFra, Now.localDate().with(TemporalAdjusters.firstDayOfMonth())).months == 3) {
                inntektslinjer.add(Inntektslinje("LOENNSINNTEKT", "trekkILoennForFerie", -25000.0,  måned, datoTil, måned.atEndOfMonth()))
            }
            return Pair(inntektslinjer, "fake respons med minus")
        } else if (fnr == "08124521514") {
            // Simulerer minus beløp for (Jon Janson Minus Beløp) i test data
            val inntektslinjer = ArrayList<Inntektslinje>()
            val måned = YearMonth.of(datoFra.year, datoFra.month)
            inntektslinjer.add(Inntektslinje("LOENNSINNTEKT", "fastloenn", 20000.0,  måned, datoTil, måned.atEndOfMonth()))
            if (Math.random() > 0.5) {
                inntektslinjer.add(Inntektslinje("LOENNSINNTEKT", "trekkILoennForFerie", -25000.0,  måned, datoTil, måned.atEndOfMonth()))
            }
            return Pair(inntektslinjer, "fake respons med mulig minus")
        } else if (fnr == "18079238011") {
            // Geir Geirsen har kun inntekter i måneden etter tilskuddsperioden
            val inntektslinjer = ArrayList<Inntektslinje>()
            val måneder = datoFra.datesUntil(datoTil, Period.ofMonths(1)).toList()
            if (måneder.size == 1) {
                return Pair(emptyList(), "fake respons med ingen inntekter i tilskuddsperioden")
            } else {
                val måned = YearMonth.of(datoFra.year, datoFra.month.plus(1))
                inntektslinjer.add(Inntektslinje("LOENNSINNTEKT", "fastloenn", 20000.0,  måned, datoTil, måned.atEndOfMonth()))
                return Pair(inntektslinjer, "fake respons med kun inntekter i måneden etter tilskuddsperioden")
            }
        } else if (fnr == "26089638754") {
            // Ferietrekk både pluss og minus
            val inntektslinjer = ArrayList<Inntektslinje>()
            val måned = YearMonth.of(datoFra.year, datoFra.month)
            inntektslinjer.add(Inntektslinje("LOENNSINNTEKT", "fastloenn", 20000.0,  måned, datoTil, måned.atEndOfMonth()))
            inntektslinjer.add(Inntektslinje("LOENNSINNTEKT", "trekkILoennForFerie",-25000.0,  måned, datoTil, måned.atEndOfMonth()))
            inntektslinjer.add(Inntektslinje("LOENNSINNTEKT", "trekkILoennForFerie",20000.0,  måned, datoTil, måned.atEndOfMonth()))
            return Pair(inntektslinjer, "fake respons med inntekter og ferietrekk både minus og pluss")

        } else if (fnr == "23039648083") {
            val inntektslinjer = ArrayList<Inntektslinje>()
            val måned = YearMonth.of(datoFra.year, datoFra.month)
            inntektslinjer.add(Inntektslinje("LOENNSINNTEKT", "fastloenn", 20000.0,  måned, datoTil, måned.atEndOfMonth()))
            inntektslinjer.add(Inntektslinje("LOENNSINNTEKT", "trekkILoennForFerie",20000.0,  måned, datoTil, måned.atEndOfMonth()))
            return Pair(inntektslinjer, "fake respons med inntekter og ferietrekk bare pluss")

        } else if (fnr == "08098138758") {
            val inntektslinjer = ArrayList<Inntektslinje>()
            val måned = YearMonth.of(datoFra.year, datoFra.month)
            inntektslinjer.add(Inntektslinje("LOENNSINNTEKT", "fastloenn", 200000.0,  måned, datoTil, måned.atEndOfMonth()))
            return Pair(inntektslinjer, "fake respons med veldig høy lønn")
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
            inntektslinjer.add(Inntektslinje("LOENNSINNTEKT", "uregelmessigeTilleggKnyttetTilArbeidetTid", 10000.0, måned, it, måned.atEndOfMonth()))
//            inntektslinjer.add(Inntektslinje("LOENNSINNTEKT", "fastTillegg", 10000.0, måned, null, null))
//            inntektslinjer.add(Inntektslinje("LOENNSINNTEKT", "overtidsgodtgjoerelse", 7683.0, måned, it, måned.atEndOfMonth()))
            inntektslinjer.add(Inntektslinje("LOENNSINNTEKT", "loennUtbetaltAvVeldedigEllerAllmennyttigInstitusjonEllerOrganisasjon", 423.0, måned, it, måned.atEndOfMonth()))
        }
        return Pair(inntektslinjer, "fake respons")
    }
}

