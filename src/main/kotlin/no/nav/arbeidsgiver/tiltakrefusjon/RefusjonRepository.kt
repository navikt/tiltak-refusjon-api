package no.nav.arbeidsgiver.tiltakrefusjon

import no.nav.arbeidsgiver.tiltakrefusjon.domain.Refusjon
import org.springframework.data.jpa.repository.JpaRepository

interface RefusjonRepository: JpaRepository<Refusjon, String>