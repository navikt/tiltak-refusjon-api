package no.nav.arbeidsgiver.tiltakrefusjon.okonomi

interface KontoregisterService {
    fun hentBankkontonummer(
            bedriftNr: String
    ): String
}