package no.nav.arbeidsgiver.tiltakrefusjon.refusjon.nydatamodell

import com.github.guepardoapps.kulid.ULID
import javax.persistence.*

@Entity
data class Inntektsgrunnlag(
        @Id
        val id: String = ULID.random(),
        @OneToMany(mappedBy = "inntektsgrunnlag", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.EAGER)
        val inntekter: List<InntektslinjeEntity>
)