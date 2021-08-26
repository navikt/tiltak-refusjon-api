package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import com.fasterxml.jackson.annotation.JsonIgnore
import com.github.guepardoapps.kulid.ULID
import no.nav.arbeidsgiver.tiltakrefusjon.utils.Now
import java.time.LocalDateTime
import javax.persistence.*

@Entity
data class Inntektsgrunnlag(
        @OneToMany(mappedBy = "inntektsgrunnlag", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.EAGER)
        val inntekter: Set<Inntektslinje>,
        @JsonIgnore
        val respons: String?
) {
    constructor(inntekter: List<Inntektslinje>, respons: String?) : this(inntekter.toSet(), respons)

    @Id
    val id: String = ULID.random()
    var innhentetTidspunkt: LocalDateTime = Now.localDateTime();

    init {
        inntekter.forEach { it.setInntektsgrunnlag(this) }
    }
}