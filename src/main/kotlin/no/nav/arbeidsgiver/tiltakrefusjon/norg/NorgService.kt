package no.nav.arbeidsgiver.tiltakrefusjon.norg

interface NorgService {
    fun hentEnhetNavn(enhet: String): String?
}