package no.nav.arbeidsgiver.tiltakrefusjon.audit

interface AuditerbarEntitet {
    val id: String
    fun getFnrOgBedrift(): FnrOgBedrift
}
