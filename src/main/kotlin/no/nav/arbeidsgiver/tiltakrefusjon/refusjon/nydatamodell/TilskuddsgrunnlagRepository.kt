package no.nav.arbeidsgiver.tiltakrefusjon.refusjon.nydatamodell

import org.springframework.data.jpa.repository.JpaRepository

interface TilskuddsgrunnlagRepository : JpaRepository<Tilskuddsgrunnlag, String> {
}