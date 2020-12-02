package no.nav.arbeidsgiver.tiltakrefusjon.refusjon.nydatamodell

import com.github.guepardoapps.kulid.ULID
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import javax.persistence.*
import kotlin.math.roundToInt
import kotlin.streams.toList

@Entity
@Table(name = "inntektslinje")
data class Inntektslinje(
        val inntektType: String,
        val beløp: Double,
        val måned: YearMonth,
        val opptjeningsperiodeFom: LocalDate?,
        val opptjeningsperiodeTom: LocalDate?
) {
    @Id
    val id: String = ULID.random()

    @ManyToOne
    @JoinColumn(name = "inntektsgrunnlag_id")
    internal lateinit var inntektsgrunnlag: Inntektsgrunnlag

    @Transient
    val erLønnsinntekt = inntektType == "LOENNSINNTEKT" && beløp > 0.0
}

fun erHverdag(dato: LocalDate) = dato.dayOfWeek != DayOfWeek.SATURDAY && dato.dayOfWeek != DayOfWeek.SUNDAY