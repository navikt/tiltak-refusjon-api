package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import no.nav.arbeidsgiver.tiltakrefusjon.utils.ulid
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
data class Tilskuddsgrunnlag(
    val avtaleId: String,
    val avtaleFom: LocalDate? = null,
    val avtaleTom: LocalDate? = null,
    val tilskuddsperiodeId: String,
    val deltakerFornavn: String,
    val deltakerEtternavn: String,
    val deltakerFnr: String,
    val arbeidsgiverFornavn: String,
    val arbeidsgiverEtternavn: String,
    val arbeidsgiverTlf: String,
    val veilederNavIdent: String,
    val bedriftNavn: String,
    val bedriftNr: String,
    val tilskuddFom: LocalDate,
    var tilskuddTom: LocalDate,
    val feriepengerSats: Double,
    val otpSats: Double,
    val arbeidsgiveravgiftSats: Double,
    @Enumerated(EnumType.STRING)
    val tiltakstype: Tiltakstype,
    var tilskuddsbeløp: Int,
    val lønnstilskuddsprosent: Int,
    val avtaleNr: Int,
    val løpenummer: Int,
    val resendingsnummer: Int? = null,
    val enhet: String?,
    val godkjentAvBeslutterTidspunkt: LocalDateTime
) {
    @Id
    val id: String = ulid()
}