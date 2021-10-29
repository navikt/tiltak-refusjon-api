package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

enum class BetalingStatus(val verdi:String) {
    UTBETALT("RECONCILED"),FEILET("VOIDED");

    companion object{
    fun fra(oebsStatus:String):BetalingStatus{
        return values().first { it.verdi.equals(oebsStatus) }
    }
    }

}