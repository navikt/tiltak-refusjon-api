package no.nav.arbeidsgiver.tiltakrefusjon.hendelseslogg

import org.springframework.data.jpa.repository.JpaRepository

interface HendelsesloggRepository : JpaRepository<Hendelseslogg, String>