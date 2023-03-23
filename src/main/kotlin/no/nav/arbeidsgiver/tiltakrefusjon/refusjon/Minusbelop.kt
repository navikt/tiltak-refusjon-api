package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import com.github.guepardoapps.kulid.ULID
import org.apache.kafka.common.protocol.types.Field.Bool
import javax.persistence.Entity
import javax.persistence.Id

@Entity
class Minusbelop(
    val avtaleNr: Int,
    var beløp: Int? = null,
    var løpenummer: Int? = null,
    var gjortOpp: Boolean = false
) {
    @Id
    val id: String = ULID.random()
}
