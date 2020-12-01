package no.nav.arbeidsgiver.tiltakrefusjon.refusjon.nydatamodell

import org.springframework.data.jpa.repository.JpaRepository

interface InntektsgrunnlagRepository : JpaRepository<Inntektsgrunnlag, String> {
}