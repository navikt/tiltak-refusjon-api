package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import com.github.guepardoapps.kulid.ULID
import jakarta.persistence.Entity
import jakarta.persistence.Id

@Entity
data class Beregning(
        val lønn: Int,
        val lønnFratrukketFerie: Int,
        val feriepenger: Int,
        val tjenestepensjon: Int,
        val arbeidsgiveravgift: Int,
        val sumUtgifter: Int,
        val beregnetBeløp: Int,
        val refusjonsbeløp: Int,
        val overTilskuddsbeløp: Boolean,
        val tidligereUtbetalt: Int,
        val fratrekkLønnFerie: Int,
        val tidligereRefundertBeløp: Int,
        val sumUtgifterFratrukketRefundertBeløp: Int,
        val overFemGrunnbeløp: Boolean? = false
) {
    @Id
    val id: String = ULID.random()
}
