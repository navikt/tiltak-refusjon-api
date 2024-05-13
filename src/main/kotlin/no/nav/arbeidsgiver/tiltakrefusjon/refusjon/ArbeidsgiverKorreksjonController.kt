package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import no.nav.arbeidsgiver.tiltakrefusjon.audit.AuditLogging
import no.nav.arbeidsgiver.tiltakrefusjon.autorisering.InnloggetBrukerService
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

const val REQUEST_MAPPING_ARBEIDSGIVER_KORREKSJON = "/api/arbeidsgiver/korreksjon"

@RestController
@RequestMapping(REQUEST_MAPPING_ARBEIDSGIVER_KORREKSJON)
@ProtectedWithClaims(issuer = "tokenx")
class ArbeidsgiverKorreksjonController(
    val innloggetBrukerService: InnloggetBrukerService,
) {
    @AuditLogging("Hent detaljer om korreksjon")
    @GetMapping("/{id}")
    fun hent(@PathVariable id: String): Korreksjon? {
        val arbeidsgiver = innloggetBrukerService.hentInnloggetArbeidsgiver()
        return arbeidsgiver.finnKorreksjon(id)
    }

}
