package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import no.nav.arbeidsgiver.tiltakrefusjon.audit.AuditLogger
import no.nav.arbeidsgiver.tiltakrefusjon.autorisering.InnloggetBrukerService
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.http.HttpMethod
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.net.URI

const val REQUEST_MAPPING_ARBEIDSGIVER_KORREKSJON = "/api/arbeidsgiver/korreksjon"

@RestController
@RequestMapping(REQUEST_MAPPING_ARBEIDSGIVER_KORREKSJON)
@ProtectedWithClaims(issuer = "tokenx")
class ArbeidsgiverKorreksjonController(
    val innloggetBrukerService: InnloggetBrukerService,
    val auditLogger: AuditLogger
) {
    @GetMapping("/{id}")
    fun hent(@PathVariable id: String): Korreksjon? {
        val arbeidsgiver = innloggetBrukerService.hentInnloggetArbeidsgiver()
        val korreksjon = arbeidsgiver.finnKorreksjon(id)

        auditLogger.logg(arbeidsgiver.identifikator, "Hent korreksjon for refusjon", URI.create("$REQUEST_MAPPING_ARBEIDSGIVER_KORREKSJON/ID"), HttpMethod.GET, korreksjon)

        return korreksjon
    }

}
