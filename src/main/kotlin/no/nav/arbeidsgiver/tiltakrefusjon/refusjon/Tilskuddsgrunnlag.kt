package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import com.github.guepardoapps.kulid.ULID
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.Tiltakstype
import java.time.LocalDate
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
        val bedriftKontonummer: String
) {
    @Id
    val id: String = ULID.random()
}