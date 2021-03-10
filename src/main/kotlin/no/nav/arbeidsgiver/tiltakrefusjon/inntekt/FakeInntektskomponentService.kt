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
    ): List<Inntektslinje> {
        if (fnr == "07098142678") {
            return emptyList()
        }
        return datoFra.datesUntil(datoTil, Period.ofMonths(1)).map {
            val måned = YearMonth.of(it.year, it.month)
            Inntektslinje("LOENNSINNTEKT", 25000.0, måned, it, måned.atEndOfMonth())
        }.toList()
    }
}