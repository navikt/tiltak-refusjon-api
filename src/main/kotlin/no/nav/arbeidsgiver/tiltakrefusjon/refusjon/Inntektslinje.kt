package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import com.fasterxml.jackson.annotation.JsonProperty
import com.github.guepardoapps.kulid.ULID
import no.nav.arbeidsgiver.tiltakrefusjon.utils.YearMonthDateAttributeConverter
import java.time.LocalDate
import java.time.YearMonth
import javax.persistence.*

@Entity
@Table(name = "inntektslinje")
data class Inntektslinje(
    val inntektType: String,
    val beskrivelse: String?,
    val beløp: Double,
    @Convert(converter = YearMonthDateAttributeConverter::class)
    val måned: YearMonth,
    val opptjeningsperiodeFom: LocalDate?,
    val opptjeningsperiodeTom: LocalDate?,
    var skalRefunderes: Boolean? = null,
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
    fun erMedIInntektsgrunnlag() =
        inntektType == "LOENNSINNTEKT" && inkluderteLønnsbeskrivelser.contains(beskrivelse)

    @JsonProperty
    fun skalTrekkesIfraInntektsgrunnlag() =
        inntektType == "LOENNSINNTEKT" && inkluderteFratrekkbeskrivelser.contains(beskrivelse)
}

val inkluderteFratrekkbeskrivelser = listOf<String>("trekkILoennForFerie")

val inkluderteLønnsbeskrivelser = listOf("fastloenn", "timeloenn", "fastTillegg")