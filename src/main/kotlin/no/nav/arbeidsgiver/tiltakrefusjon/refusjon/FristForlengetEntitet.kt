package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import jakarta.persistence.Entity
import jakarta.persistence.Id
import no.nav.arbeidsgiver.tiltakrefusjon.utils.Now
import no.nav.arbeidsgiver.tiltakrefusjon.utils.ulid
import java.time.LocalDate
import java.time.LocalDateTime

@Entity(name = "frist_forlenget")
data class FristForlengetEntitet(val refusjonId: String, val gammelFrist: LocalDate, val nyFrist: LocalDate, val årsak: String, val utførtAv: String) {
    @Id
    val id: String = ulid()
    val tidspunkt: LocalDateTime = Now.localDateTime()
}
