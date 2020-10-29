package no.nav.arbeidsgiver.tiltakrefusjon.autorisering

import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.Refusjon

abstract class InnloggetBruker(){
    abstract fun finnAlle(): List<Refusjon>
    abstract fun finnAlleMedBedriftnummer(bedriftnummer: String):List<Refusjon>
    abstract fun finnRefusjon(id: String): Refusjon?

}