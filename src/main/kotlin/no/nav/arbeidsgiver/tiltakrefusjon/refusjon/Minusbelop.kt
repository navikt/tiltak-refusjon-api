package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import com.github.guepardoapps.kulid.ULID
import jakarta.persistence.Entity
import jakarta.persistence.Id

@Entity
class Minusbelop(
    val avtaleNr: Int,
    var beløp: Int? = null,
    var løpenummer: Int? = null,
    var gjortOpp: Boolean = false,
    var gjortOppAvRefusjonId: String? = null
) {
    @Id
    val id: String = ULID.random()
}
