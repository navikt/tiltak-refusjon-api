package no.nav.arbeidsgiver.tiltakrefusjon.autorisering

import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.nydatamodell.Refusjonsak

abstract class InnloggetBruker {
    abstract fun finnAlle(): List<Refusjonsak>
    abstract fun finnAlleMedBedriftnummer(bedriftnummer: String): List<Refusjonsak>
    abstract fun finnRefusjonsak(id: String): Refusjonsak?
}