package no.nav.arbeidsgiver.tiltakrefusjon.domain

import javax.persistence.Entity
import javax.persistence.Id

@Entity
data class Refusjon(
        @Id
        val id: String
)




