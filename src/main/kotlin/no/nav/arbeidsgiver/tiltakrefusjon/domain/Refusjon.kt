package no.nav.arbeidsgiver.tiltakrefusjon.domain

import java.time.LocalDate
import java.time.LocalDateTime
import javax.persistence.Entity
import javax.persistence.Id

@Entity
data class Refusjon(
        @Id
        val id: String,
        val tiltak: String? = null,
        val deltaker: String? = null,
        val deltakerFnr: String? = null,
        val veileder: String? = null,
        val bedrift: String? = null,
        val bedriftnummer: String? = null,
        var feriedager: Int? = null,
        val trekkFeriedagerBeløp: Int? = null,
        val sykedager: Int? = null,
        val sykepenger: Int? = null,
        val stillingsprosent: Int? = null,
        val månedslønn: Int? = null,
        val nettoMånedslønn: Int? = null,
        val satsOtp: Double? = null,
        val beløpOtp: Int? = null,
        val satsFeriepenger: Double? = null,
        val feriepenger: Int? = null,
        val satsArbeidsgiveravgift: Double? = null,
        val arbeidsgiveravgift: Int? = null,
        val sumUtgifterArbeidsgiver: Int? = null,
        val satsRefusjon: Double? = null,
        val refusjonPrMåned: Int? = null,
        val fraDato: LocalDate? = null,
        val tilDato: LocalDate? = null,
        val opprettet_tidspunkt: LocalDateTime? = null
)


