package no.nav.arbeidsgiver.tiltakrefusjon.refusjon.nydatamodell

import com.github.guepardoapps.kulid.ULID
import java.time.LocalDateTime
import javax.persistence.*

@Entity
data class Inntektsgrunnlag(
        @OneToMany(mappedBy = "inntektsgrunnlag", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.EAGER)
        val inntekter: List<Inntektslinje>
) {
    @Id
    val id: String = ULID.random()
    val innhentetTidspunkt: LocalDateTime = LocalDateTime.now()

    init {
        inntekter.forEach { it.inntektsgrunnlag = this }
    }
}