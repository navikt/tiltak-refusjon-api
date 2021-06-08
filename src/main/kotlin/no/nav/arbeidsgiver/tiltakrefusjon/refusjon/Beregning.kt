package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import com.github.guepardoapps.kulid.ULID
import javax.persistence.Entity
import javax.persistence.Id

@Entity
data class Beregning(
        val lønn: Int,
        val feriepenger: Int,
        val tjenestepensjon: Int,
        val arbeidsgiveravgift: Int,
        val sumUtgifter: Int,
        val beregnetBeløp: Int,
        val refusjonsbeløp: Int,
        val overTilskuddsbeløp: Boolean,
        val commitHash: String = ""
) {
    @Id
    val id: String = ULID.random()
}
