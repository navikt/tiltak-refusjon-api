package no.nav.arbeidsgiver.tiltakrefusjon.inntekt

import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.Inntektslinje
import java.time.LocalDate

interface InntektskomponentService {
    fun hentInntekter(
        fnr: String,
        bedriftnummerDetSøkesPå: String,
        datoFra: LocalDate,
        datoTil: LocalDate
    ): List<Inntektslinje>
}