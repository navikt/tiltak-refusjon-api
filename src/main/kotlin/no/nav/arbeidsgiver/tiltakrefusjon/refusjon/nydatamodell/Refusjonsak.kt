package no.nav.arbeidsgiver.tiltakrefusjon.refusjon.nydatamodell

import com.github.guepardoapps.kulid.ULID
import java.time.Instant
import javax.persistence.*

@Entity
data class Refusjonsak(
        @OneToOne(orphanRemoval = true, cascade = [CascadeType.ALL])
        val tilskuddsgrunnlag: Tilskuddsgrunnlag,
        val bedriftNr: String,
        val deltakerFnr: String
) {
    @Id
    val id: String = ULID.random()

    @OneToOne(orphanRemoval = true, cascade = [CascadeType.ALL])
    var inntektsgrunnlag: Inntektsgrunnlag? = null

    @Enumerated(EnumType.STRING)
    var status: RefusjonStatus = RefusjonStatus.UBEHANDLET
    var godkjentAvArbeidsgiver: Instant? = null
    var godkjentAvSaksbehandler: Instant? = null
    var refusjonsbeløp: Int? = null
    var commitHash: String? = null
}

enum class RefusjonStatus {
    UBEHANDLET, BEHANDLET
}
