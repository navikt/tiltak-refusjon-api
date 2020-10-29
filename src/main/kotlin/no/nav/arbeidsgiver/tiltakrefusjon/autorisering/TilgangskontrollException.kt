package no.nav.arbeidsgiver.tiltakrefusjon.autorisering

import org.springframework.http.HttpStatus
import org.springframework.web.client.HttpStatusCodeException

class TilgangskontrollException(status: HttpStatus) : HttpStatusCodeException(status)