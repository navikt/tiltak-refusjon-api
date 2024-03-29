package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import com.fasterxml.jackson.annotation.JsonProperty
import no.nav.arbeidsgiver.tiltakrefusjon.utils.YearMonthDateAttributeConverter
import java.time.LocalDate
import java.time.YearMonth
import jakarta.persistence.*
import no.nav.arbeidsgiver.tiltakrefusjon.utils.ulid

@Entity
@Table(name = "inntektslinje")
data class Inntektslinje(
    val inntektType: String,
    val beskrivelse: String?,
    var beløp: Double,
    @Convert(converter = YearMonthDateAttributeConverter::class)
    val måned: YearMonth,
    val opptjeningsperiodeFom: LocalDate?,
    val opptjeningsperiodeTom: LocalDate?,
    @Column(name = "er_opptjent_i_periode")
    var erOpptjentIPeriode: Boolean? = null,
) {
    @Id
    val id: String = ulid()

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
    fun skalTrekkesIfraInntektsgrunnlag(tilskuddFom: LocalDate): Boolean {
        val skalTrekkes = inntektType == "LOENNSINNTEKT" && inkluderteFratrekkbeskrivelser.contains(beskrivelse) && måned == YearMonth.from(tilskuddFom)
        return skalTrekkes
    }

}

val inkluderteFratrekkbeskrivelser = listOf<String>("trekkILoennForFerie")
// Dette er verdier som settes av inntektskomponenten
// https://github.com/navikt/inntektskomponenten/blob/a2ea187cc77c43ef696ecba98b7b06f69ebc75d6/inntektskomponenten-core/src/main/java/no/nav/inntektskomponenten/domain/kodeverk/beskrivelser/LoennsinntektBeskrivelser.java
val inkluderteLønnsbeskrivelser = listOf(
    "fastloenn",
    "timeloenn",
    "fastTillegg",
    "uregelmessigeTilleggKnyttetTilArbeidetTid",
    "loennUtbetaltAvVeldedigEllerAllmennyttigInstitusjonEllerOrganisasjon",
    "loennEtterDoedsfall"
)