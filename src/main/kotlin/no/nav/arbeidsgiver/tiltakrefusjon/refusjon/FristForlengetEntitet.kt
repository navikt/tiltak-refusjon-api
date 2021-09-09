package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import com.github.guepardoapps.kulid.ULID
import no.nav.arbeidsgiver.tiltakrefusjon.utils.Now
import java.time.LocalDateTime
import java.time.LocalDate
import javax.persistence.Entity
import javax.persistence.Id

@Entity(name = "frist_forlenget")
data class FristForlengetEntitet(val refusjonId: String, val gammelFrist: LocalDate, val nyFrist: LocalDate, val årsak: String, val utførtAv: String) {
    @Id
    val id: String = ULID.random()
    val tidspunkt: LocalDateTime = Now.localDateTime()
}
