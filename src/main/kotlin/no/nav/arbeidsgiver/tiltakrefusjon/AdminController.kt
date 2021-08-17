package no.nav.arbeidsgiver.tiltakrefusjon

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.RefusjonService
import org.springframework.web.bind.annotation.RestController

@RestController
class AdminController(val service: RefusjonService, val objectMapper: ObjectMapper) {







}