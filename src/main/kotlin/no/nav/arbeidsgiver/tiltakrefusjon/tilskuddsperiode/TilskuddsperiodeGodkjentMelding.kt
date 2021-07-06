package no.nav.arbeidsgiver.tiltakrefusjon.tilskuddsperiode

import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.Tiltakstype
import java.time.LocalDate

data class TilskuddsperiodeGodkjentMelding(
        val avtaleId: String,
        val tilskuddsperiodeId: String,
        val avtaleInnholdId: String,
        val tiltakstype: Tiltakstype,
        val deltakerFornavn: String,
        val deltakerEtternavn: String,
        val deltakerFnr: String,
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
        val enhet: String?,
)