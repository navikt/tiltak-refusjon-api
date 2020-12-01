package no.nav.arbeidsgiver.tiltakrefusjon.refusjon.nydatamodell

import com.github.guepardoapps.kulid.ULID
import java.time.LocalDate
import java.time.YearMonth
import javax.persistence.*

@Entity
@Table(name = "inntektslinje")
data class InntektslinjeEntity(
        @Id
        val id: String = ULID.random(),
        val inntektType: String,
        val beløp: Double,
        val måned: YearMonth,
        val opptjeningsperiodeFom: LocalDate?,
        val opptjeningsperiodeTom: LocalDate?
) {
    @ManyToOne
    @JoinColumn(name = "inntektsgrunnlag_id")
    lateinit var inntektsgrunnlag: Inntektsgrunnlag
}