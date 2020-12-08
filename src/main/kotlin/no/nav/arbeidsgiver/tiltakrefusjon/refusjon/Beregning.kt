package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import com.github.guepardoapps.kulid.ULID
import javax.persistence.Entity
import javax.persistence.Id

@Entity
data class Beregning(
        val refusjonsbeløp: Int,
        val commitHash: String = ""
) {
    @Id
    val id: String = ULID.random()
}
