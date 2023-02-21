package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import com.github.guepardoapps.kulid.ULID
import java.time.LocalDate
import java.time.LocalDateTime
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.Id

@Entity
data class Tilskuddsgrunnlag(
    val avtaleId: String,
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
    val resendingsNummer: Int?,
    val enhet: String?,
    val godkjentAvBeslutterTidspunkt: LocalDateTime
) {
    @Id
    val id: String = ULID.random()
}