package no.nav.arbeidsgiver.tiltakrefusjon.domain

data class Refusjon(
        val id: String,
        val tiltakstype: String,
        val deltakerNavn: String,
        val veilederNavn: String,
        val bedriftNavn: String,
        val bedriftKontaktperson: String,
        val feriedager: Int,
        val trekkFeriedager: Int,
        val sykedager: Int,
        val sykepenger: Int,
        val stillingsprosent: Int,
        val manedslonn: Int,
        val nettoManedslonn: Int,
        val satsOtp: Double,
        val otpBelop: Int,
        val satsFeriepenger: Double,
        val feriepenger: Int,
        val satsArbgiverAvgift: Double,
        val arbgiverAvgift: Int,
        val totalArbgiverUtgift: Int,
        val refusjonsProsent: Int,
        val refusjonsBelop: Int,
        val varighet: Varighet
)




