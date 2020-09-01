package no.nav.arbeidsgiver.tiltakrefusjon

import no.nav.arbeidsgiver.tiltakrefusjon.domain.Refusjon
import java.time.LocalDate
import java.time.LocalDateTime

fun enRefusjon():Refusjon{
    return Refusjon(
            id = "1",
            tiltak = "Arbeidstrening",
            deltaker = "Mikke Mus",
            deltakerFnr = "07098142678",
            veileder = "Jonas Trane",
            bedrift = "Kiwi Majorstuen",
            bedriftnummer = "998877665",
            feriedager = 2,
            trekkFeriedagerBeløp = 1500,
            sykedager = 2,
            sykepenger = 2000,
            stillingsprosent = 100,
            månedslønn = 30000,
            nettoMånedslønn = 26500,
            satsOtp = 0.02,
            beløpOtp = 530,
            satsFeriepenger = 0.12,
            feriepenger = 3180,
            satsArbeidsgiveravgift = 0.141,
            arbeidsgiveravgift = 3737,
            sumUtgifterArbeidsgiver = 33947,
            satsRefusjon = 0.4,
            refusjonPrMåned = 13579,
            fraDato = LocalDate.of(2020, 8, 1),
            tilDato = LocalDate.of(2020, 10, 31),
            opprettet_tidspunkt = LocalDateTime.now()

    )
}

fun toRefusjoner(): List<Refusjon> {

    val refusjon1 = enRefusjon()
    val refusjon2 = Refusjon(
            id = "2",
            tiltak = "Arbeidstrening",
            deltaker = "Donald Duck",
            deltakerFnr = "07049223182",
            veileder = "Alf Hansen",
            bedrift = "Kiwi Majorstuen",
            bedriftnummer = "998877665",
            feriedager = 0,
            trekkFeriedagerBeløp = 0,
            sykedager = 0,
            sykepenger = 0,
            stillingsprosent = 50,
            månedslønn = 20000,
            nettoMånedslønn = 15000,
            satsOtp = 0.02,
            beløpOtp = 530,
            satsFeriepenger = 0.12,
            feriepenger = 3180,
            satsArbeidsgiveravgift = 0.141,
            arbeidsgiveravgift = 2737,
            sumUtgifterArbeidsgiver = 33947,
            satsRefusjon = 0.4,
            refusjonPrMåned = 10579,
            fraDato = LocalDate.of(2020, 8, 1),
            tilDato = LocalDate.of(2020, 11, 1),
            opprettet_tidspunkt = LocalDateTime.now()
    )
    return listOf(refusjon1, refusjon2)
}
