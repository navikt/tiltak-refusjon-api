package no.nav.arbeidsgiver.tiltakrefusjon.okonomi.prodtest

interface KontoregisterService2 {
    fun hentBankkontonummer(
            bedriftNr: String
    ): String?
}