package no.nav.arbeidsgiver.tiltakrefusjon.refusjon.nydatamodell

import java.time.Instant
import javax.persistence.Entity
import javax.persistence.Id

@Entity
data class Refusjonsak(
        @Id
        val id: String,
        val avtalegrunnlagId: String,
        val inntektsgrunnlagId: String,
        val refusjonsbeløp: String,
        val status: String,
        val godkjentAvArbeidsgiver: Instant,
        val godkjentAvSaksbehandler: Instant
)