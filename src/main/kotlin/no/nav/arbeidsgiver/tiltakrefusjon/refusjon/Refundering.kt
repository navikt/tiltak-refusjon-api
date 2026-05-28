package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

/**
 * Korreksjoner og refusjoner blir ofte behandlet likt, feks for beregninger.
 */
sealed interface Refundering {
    val refusjonsgrunnlag: Refusjonsgrunnlag
    val status: RefunderingStatus
    val deltakerFnr: String
    val bedriftNr: String
    fun tiltakstype(): Tiltakstype
}

