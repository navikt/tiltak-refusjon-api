package no.nav.arbeidsgiver.tiltakrefusjon.domain

import com.fasterxml.jackson.annotation.JsonIgnore
import java.time.LocalDate
import javax.persistence.Entity
import javax.persistence.Id

@Entity
data class Refusjon(
        @Id
        val id: String,
        val tiltak: String,
        val deltaker: String,
        val deltakerFnr: String,
        val veileder: String,
        val bedrift: String,
        val bedriftnummer: String,
        val feriedager: Int,
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

        @JsonIgnore
        @Transient
        var varighet: Varighet?
)

fun datoerTilVarighet(fraDato: LocalDate, tilDato: LocalDate): Varighet{
        return Varighet(fraDato, tilDato);
}



