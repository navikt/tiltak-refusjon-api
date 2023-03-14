package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import com.github.guepardoapps.kulid.ULID
import javax.persistence.Entity
import javax.persistence.Id

@Entity
class Minusbelop(
    val avtaleNr: Int,
    var beløp: Int? = null,
    var løpenummer: Int? = null
) {
    @Id
    val id: String = ULID.random()
}
