package no.nav.arbeidsgiver.tiltakrefusjon.domain

import java.time.LocalDate

class FakeRefusjon(
        val id: String = "1",
        val tiltakstype: String = "Arbeidstrening",
        val deltakerNavn: String = "Mikke Mus",
        val veilederNavn: String = "Jonas Trane",
        val bedriftNavn: String = "Kiwi Majorstuen",
        val bedriftKontaktperson: String = "Martine Loren",
        val feriedager: Int = 2,
        val trekkFeriedager: Int = 1500,
        val sykedager: Int = 2,
        val sykepenger: Int = 2000,
        val stillingsprosent: Int = 100,
        val manedslonn: Int = 30000,
        val nettoManedslonn: Int = 26500,
        val satsOtp: Double = 0.02,
        val otpBelop: Int = 530,
        val satsFeriepenger: Double = 0.12,
        val feriepenger: Int = 3180,
        val satsArbgiverAvgift: Double = 0.141,
        val arbgiverAvgift: Int = 3737,
        val totalArbgiverUtgift: Int = 33947,
        val refusjonsProsent: Int = 40,
        val refusjonsBelop: Int = 13579,
        val varighet: Varighet = Varighet(LocalDate.now(), LocalDate.now().plusMonths(3))
)