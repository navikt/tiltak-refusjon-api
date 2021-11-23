package no.nav.arbeidsgiver.tiltakrefusjon.organisasjon

data class Virksomhet(
        val juridiskEnhetOrganisasjonsnummer: String = "",
        val bedriftNr: String = "",
        val bedriftGatenavn: String = ".",
        val bedriftPostnummer: String = "",
        val navnPåJuridiskEnhet: String = ""
) {
    fun harBedriftAdresseOgJuridiskEnhet(): Boolean {
        return juridiskEnhetOrganisasjonsnummer.isNotEmpty()
                && bedriftPostnummer.isNotEmpty()
                && navnPåJuridiskEnhet.isNotEmpty()
    }
}