package no.nav.arbeidsgiver.tiltakrefusjon.okonomi

interface KontoregisterkomponentService {
    fun hentBankkontonummer(
        bedriftNr: String
    ): String
}