package no.nav.arbeidsgiver.tiltakrefusjon.varsling

import com.github.guepardoapps.kulid.ULID
import java.time.LocalDateTime
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id

@Entity
data class Varsling(
    val refusjonId: String,
    @Enumerated(EnumType.STRING)
    val varselType: VarselType,
    val varselTidspunkt: LocalDateTime,
) {
    @Id
    val id: String = ULID.random()
}