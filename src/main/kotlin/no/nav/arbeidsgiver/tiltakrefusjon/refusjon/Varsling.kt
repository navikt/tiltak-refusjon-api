package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import com.github.guepardoapps.kulid.ULID
import java.time.LocalDateTime
import javax.persistence.Entity
import javax.persistence.Id

@Entity
data class Varsling(val refusjonId: String, val varselType: VarselType, val varselTidspunkt: LocalDateTime) {
    @Id
    val id: String = ULID.random()
}