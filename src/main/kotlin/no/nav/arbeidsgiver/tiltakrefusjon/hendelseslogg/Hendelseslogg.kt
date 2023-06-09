package no.nav.arbeidsgiver.tiltakrefusjon.hendelseslogg

import com.github.guepardoapps.kulid.ULID
import jakarta.persistence.Entity
import jakarta.persistence.Id
import no.nav.arbeidsgiver.tiltakrefusjon.utils.Now
import java.time.LocalDateTime

@Entity
class Hendelseslogg(
    val appImageId: String,
    val refusjonId: String,
    val korreksjonId: String?,
    val utførtAv: String,
    val event: String,
) {
    @Id
    val id: String = ULID.random()
    val tidspunkt: LocalDateTime = Now.localDateTime()
}