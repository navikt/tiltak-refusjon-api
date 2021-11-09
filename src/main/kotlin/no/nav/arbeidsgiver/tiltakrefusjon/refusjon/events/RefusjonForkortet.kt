package no.nav.arbeidsgiver.tiltakrefusjon.refusjon.events

import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.Refusjon

data class RefusjonForkortet(override val refusjon: Refusjon, override val utførtAv: String = "Kafka") :
    SporbarRefusjonHendelse
