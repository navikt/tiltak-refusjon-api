package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import no.nav.arbeidsgiver.tiltakrefusjon.tilskudd.TilskuddMelding
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*
import javax.persistence.Entity
import javax.persistence.Id

@Entity
data class Refusjon(
        @Id
        val id: String,
        val deltaker: String,
        val tiltakstype: Tiltakstype,
        val status: Status,
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
        val opprettetTidspunkt: LocalDateTime
)


