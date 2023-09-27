package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import com.fasterxml.jackson.annotation.JsonIgnore
import no.nav.arbeidsgiver.tiltakrefusjon.utils.Now
import java.time.LocalDateTime
import jakarta.persistence.*
import no.nav.arbeidsgiver.tiltakrefusjon.utils.ulid

@Entity
data class Inntektsgrunnlag(
        @OneToMany(mappedBy = "inntektsgrunnlag", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.EAGER)
        val inntekter: Set<Inntektslinje>,
        @JsonIgnore
        val respons: String?,
) {
    val bruttoLønn: Double? = inntekter.filter { it.erMedIInntektsgrunnlag() }.sumOf { it.beløp }
    constructor(inntekter: List<Inntektslinje>, respons: String?) : this(inntekter.toSet(), respons)

    @Id
    val id: String = ulid()
    var innhentetTidspunkt: LocalDateTime = Now.localDateTime()

    init {
        inntekter.forEach { it.setInntektsgrunnlag(this) }
    }
}