package no.nav.arbeidsgiver.tiltakrefusjon.hendelseslogg

import com.github.guepardoapps.kulid.ULID
import no.nav.arbeidsgiver.tiltakrefusjon.utils.Now
import java.time.LocalDateTime
import javax.persistence.Entity
import javax.persistence.Id

@Entity
class Hendelseslogg(
    val appImageId: String,
    val refusjonId: String?,
    val korreksjonId: String?,
    val utførtAv: String,
    val event: String,
) {
    @Id
    val id: String = ULID.random()
    val tidspunkt: LocalDateTime = Now.localDateTime()


    init {
        if (!((refusjonId == null) xor (korreksjonId == null))) {
            throw RuntimeException("må sette enten refusjonId eller korreksjonId")
        }
    }


}