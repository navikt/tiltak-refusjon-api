package no.nav.arbeidsgiver.tiltakrefusjon.refusjon.nydatamodell

import com.github.guepardoapps.kulid.ULID
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import javax.persistence.*
import kotlin.streams.toList

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
    internal lateinit var inntektsgrunnlag: Inntektsgrunnlag

    @Transient
    private val dagerOpptjent = (opptjeningsperiodeFom ?: måned.atDay(1)).datesUntil((opptjeningsperiodeTom
            ?: måned.atEndOfMonth()).plusDays(1)).filter(::erHverdag).toList()

    @Transient
    private val antallDagerOpptjent = dagerOpptjent.count()

    fun antallDagerOpptjent(fraDato: LocalDate, tilDato: LocalDate) = dagerOpptjent.filter { !it.isBefore(fraDato) && !it.isAfter(tilDato) }.count()

    @Transient
    var beløpPerDag = beløp / antallDagerOpptjent

    @Transient
    val erLønnsinntekt = inntektType == "LOENNSINNTEKT" && beløp > 0.0
}

fun erHverdag(dato: LocalDate) = dato.dayOfWeek != DayOfWeek.SATURDAY && dato.dayOfWeek != DayOfWeek.SUNDAY