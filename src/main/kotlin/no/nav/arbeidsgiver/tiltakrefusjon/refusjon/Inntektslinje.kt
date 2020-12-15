package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import com.fasterxml.jackson.annotation.JsonProperty
import com.github.guepardoapps.kulid.ULID
import java.time.LocalDate
import java.time.YearMonth
import javax.persistence.*

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
    private lateinit var inntektsgrunnlag: Inntektsgrunnlag

    internal fun setInntektsgrunnlag(inntektsgrunnlag: Inntektsgrunnlag) {
        this.inntektsgrunnlag = inntektsgrunnlag
    }

    @JsonProperty
    fun erLønnsinntekt() = inntektType == "LOENNSINNTEKT" && beløp > 0.0

    @JsonProperty
    fun inntektFordelesFom(): LocalDate = opptjeningsperiodeFom ?: måned.atDay(1)

    @JsonProperty
    fun inntektFordelesTom(): LocalDate = opptjeningsperiodeTom ?: måned.atEndOfMonth()
}