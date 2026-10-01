package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.persistence.CascadeType
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import no.nav.arbeidsgiver.tiltakrefusjon.utils.Now
import no.nav.arbeidsgiver.tiltakrefusjon.utils.ulid
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

@Entity
data class Inntektsgrunnlag(
        @OneToMany(mappedBy = "inntektsgrunnlag", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.EAGER)
        val inntekter: MutableSet<Inntektslinje>,
        @JsonIgnore
        val respons: String?,
) {
    constructor(inntekter: List<Inntektslinje>, respons: String?) : this(inntekter.toMutableSet(), respons)

    @Id
    val id: String = ulid()
    var innhentetTidspunkt: LocalDateTime = Now.localDateTime()
    val bruttoLønn: Double? = inntekter.filter { it.erMedIInntektsgrunnlag() }.sumOf { it.beløp }

    @get:JsonProperty
    val inntekterForPerioden: SortedSet<Inntektslinje>
        get() = inntekter
            .filter { it.erMedIInntektsgrunnlag() }
            .toSortedSet(
                compareBy<Inntektslinje, LocalDate?>(nullsLast()) { it.opptjeningsperiodeFom }
                    .thenBy(nullsLast()) { it.opptjeningsperiodeTom }
                    .thenBy(nullsLast()) { it.beskrivelse }
                    .thenBy { it.id },
            )

    fun beregnBruttolonnOpptjentIPerioden(): Double = kalkulerBruttoLønn(inntekter.toList())

    fun beregnFerietrekk(tilskuddFom: LocalDate): Double = leggSammenTrekkGrunnlag(inntekter.toList(), tilskuddFom)

    init {
        inntekter.forEach { it.setInntektsgrunnlag(this) }
    }
}
