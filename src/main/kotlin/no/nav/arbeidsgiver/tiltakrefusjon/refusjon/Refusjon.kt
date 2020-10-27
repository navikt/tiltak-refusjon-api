package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import java.time.LocalDate
import java.time.LocalDateTime
import javax.persistence.Entity
import javax.persistence.Id

@Entity
data class Refusjon(
        @Id
        val id: String,
        val deltaker: String,
        val tiltak: String,
        val deltakerFnr: String,
        val veileder: String,
        val bedrift: String,
        val bedriftnummer: String,
        var feriedager: Int,
        val trekkFeriedagerBeløp: Int,
        val sykedager: Int,
        val sykepenger: Int,
        val stillingsprosent: Int,
        val månedslønn: Int,
        val nettoMånedslønn: Int,
        val satsOtp: Double,
        val beløpOtp: Int,
        val satsFeriepenger: Double,
        val feriepenger: Int,
        val satsArbeidsgiveravgift: Double,
        val arbeidsgiveravgift: Int,
        val sumUtgifterArbeidsgiver: Int,
        val satsRefusjon: Double,
        val refusjonPrMåned: Int,
        val fraDato: LocalDate,
        val tilDato: LocalDate,
        val opprettet_tidspunkt: LocalDateTime
)

