package no.nav.arbeidsgiver.tiltakrefusjon.varsling

import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.events.SendtVarsel
import no.nav.arbeidsgiver.tiltakrefusjon.utils.ulid
import org.springframework.data.domain.AbstractAggregateRoot
import java.time.LocalDateTime

@Entity
data class Varsling(
    val refusjonId: String,
    @Enumerated(EnumType.STRING)
    val varselType: VarselType,
    val varselTidspunkt: LocalDateTime,
) : AbstractAggregateRoot<Varsling>() {
    @Id
    val id: String = ulid()
    init {
        registerEvent(SendtVarsel(this.refusjonId, this.varselType))
    }
}