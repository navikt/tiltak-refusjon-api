package no.nav.arbeidsgiver.tiltakrefusjon.varsling

import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import no.nav.arbeidsgiver.tiltakrefusjon.utils.ulid
import java.time.LocalDateTime

@Entity
data class Varsling(
    val refusjonId: String,
    @Enumerated(EnumType.STRING)
    val varselType: VarselType,
    val varselTidspunkt: LocalDateTime,
) {
    @Id
    val id: String = ulid()
}