package no.nav.arbeidsgiver.tiltakrefusjon.autorisering

import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.Identifikator
import org.springframework.http.HttpStatus
import org.springframework.web.client.HttpClientErrorException

data class NavIdent(override val verdi: String) : Identifikator {

    init {
        require(erNavIdent(verdi)) {
            throw HttpClientErrorException(HttpStatus.UNAUTHORIZED)
        }
    }

    fun erNavIdent(verdi: String?): Boolean {
        return verdi != null && verdi.matches(Regex("\\w\\d{6}"))
    }
}