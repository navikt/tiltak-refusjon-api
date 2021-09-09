package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import org.springframework.data.jpa.repository.JpaRepository

interface FristForlengetRepository : JpaRepository<FristForlengetEntitet, String>