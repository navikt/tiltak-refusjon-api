package no.nav.arbeidsgiver.tiltakrefusjon.varsling

import com.github.guepardoapps.kulid.ULID
import java.time.LocalDateTime
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.Id

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