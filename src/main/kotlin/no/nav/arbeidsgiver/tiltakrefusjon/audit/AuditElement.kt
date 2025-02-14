package no.nav.arbeidsgiver.tiltakrefusjon.audit

data class AuditElement(
    val id: String,
    val deltakerFnr: String,
    val bedrift: String
) {
    companion object {
        fun of(it: AuditerbarEntitet) = AuditElement(
            it.id,
            it.getFnrOgBedrift().deltakerFnr,
            it.getFnrOgBedrift().bedrift
        )
    }

}
