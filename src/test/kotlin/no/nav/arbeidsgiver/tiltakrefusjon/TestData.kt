package no.nav.arbeidsgiver.tiltakrefusjon

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.Refusjon
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.Status
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.Tiltakstype
import java.io.File
import java.time.LocalDate
import java.time.LocalDateTime

val TESTDATAPATH = "/src/test/resources/testdata/refusjon.json"

fun enRefusjon(): Refusjon {
    return Refusjon(
            id = "1",
            tiltakstype = Tiltakstype.MIDLERTIDLIG_LONNSTILSKUDD,
            status = Status.BEHANDLET,
            deltaker = "Mikke Mus",
            deltakerFnr = "07098142678",
            veileder = "Jonas Trane",
            bedrift = "Kiwi Majorstuen",
            bedriftnummer = "910712306",
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
            opprettetTidspunkt = LocalDateTime.now()
    )

}

fun refusjoner(): List<Refusjon> {
    val mapper = jacksonObjectMapper()
    mapper.registerKotlinModule()
    mapper.registerModule(JavaTimeModule())
    val refusjoner = mapper.readValue<List<Refusjon>>(File(System.getProperty("user.dir") + TESTDATAPATH).readText(Charsets.UTF_8))
    return refusjoner
}
