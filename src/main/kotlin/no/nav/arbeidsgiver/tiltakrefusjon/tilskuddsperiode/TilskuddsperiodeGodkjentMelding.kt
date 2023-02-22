package no.nav.arbeidsgiver.tiltakrefusjon.tilskuddsperiode

import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.Tiltakstype
import java.time.LocalDate
import java.time.LocalDateTime

data class TilskuddsperiodeGodkjentMelding(
        val avtaleId: String,
        val tilskuddsperiodeId: String,
        val avtaleInnholdId: String,
        val tiltakstype: Tiltakstype,
        val deltakerFornavn: String,
        val deltakerEtternavn: String,
        val deltakerFnr: String,
        val arbeidsgiverFornavn: String,
        val arbeidsgiverEtternavn: String,
        val arbeidsgiverTlf: String,
        val veilederNavIdent: String,
        val bedriftNavn: String,
        val bedriftNr: String,
        val tilskuddsbeløp: Int,
        val tilskuddFom: LocalDate,
        val tilskuddTom: LocalDate,
        val feriepengerSats: Double,
        val otpSats: Double,
        val arbeidsgiveravgiftSats: Double,
        val lønnstilskuddsprosent: Int,
        val avtaleNr: Int,
        val løpenummer: Int,
        val resendingsnummer: Int? = null,
        val enhet: String?,
        val godkjentTidspunkt: LocalDateTime
)