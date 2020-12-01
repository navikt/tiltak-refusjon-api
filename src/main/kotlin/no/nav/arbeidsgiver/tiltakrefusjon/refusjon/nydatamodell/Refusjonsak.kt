package no.nav.arbeidsgiver.tiltakrefusjon.refusjon.nydatamodell

import com.github.guepardoapps.kulid.ULID
import java.time.Instant
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.Id

@Entity
data class Refusjonsak(
        @Id
        val id: String = ULID.random(),
        val tilskuddsgrunnlagId: String,
        val inntektsgrunnlagId: String? = null,
        @Enumerated(EnumType.STRING)
        val status: RefusjonStatus = RefusjonStatus.UBEHANDLET,
        val godkjentAvArbeidsgiver: Instant? = null,
        val godkjentAvSaksbehandler: Instant? = null,
        val refusjonsbeløp: Int? = null,
        val commitHash: String? = null,
        val bedriftNr: String,
        val deltakerFnr: String
)

enum class RefusjonStatus {
    UBEHANDLET, BEHANDLET
}
