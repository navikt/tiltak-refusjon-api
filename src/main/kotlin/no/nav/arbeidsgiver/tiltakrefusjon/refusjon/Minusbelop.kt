package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import jakarta.persistence.Entity
import jakarta.persistence.Id
import no.nav.arbeidsgiver.tiltakrefusjon.utils.ulid

@Entity
class Minusbelop(
    val avtaleNr: Int,
    var beløp: Int? = null,
    var løpenummer: Int? = null,
    var gjortOpp: Boolean = false,
    var gjortOppAvRefusjonId: String? = null
) {
    @Id
    val id: String = ulid()
}
