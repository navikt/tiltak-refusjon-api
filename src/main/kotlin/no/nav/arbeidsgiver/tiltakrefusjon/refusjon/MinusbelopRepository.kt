package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import org.springframework.data.jpa.repository.JpaRepository

interface MinusbelopRepository : JpaRepository<Minusbelop, String> {
    fun findAllByAvtaleNr(avtaleNr: Int): List<Minusbelop>
}