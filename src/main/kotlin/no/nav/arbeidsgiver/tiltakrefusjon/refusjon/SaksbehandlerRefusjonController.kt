package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import no.nav.arbeidsgiver.tiltakrefusjon.audit.AuditLogger
import no.nav.arbeidsgiver.tiltakrefusjon.autorisering.InnloggetBrukerService
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.events.ForlengFristRequest
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.http.HttpMethod
import org.springframework.web.bind.annotation.*
import java.net.URI

const val REQUEST_MAPPING_SAKSBEHANDLER_REFUSJON = "/api/saksbehandler/refusjon"

data class HentSaksbehandlerRefusjonerQueryParametre(
    val veilederNavIdent: String? = null,
    val enhet: String? = null,
    val deltakerFnr: String? = null,
    val bedriftNr: String? = null,
    val status: RefusjonStatus? = null,
    val tiltakstype: Tiltakstype? = null,
    val avtaleNr: Int? = null,
    val page: Int = 0,
    val size: Int = 10
)

data class MerkForUnntakOmInntekterToMånederFremRequest(val merking: Int)

@RestController
@RequestMapping(REQUEST_MAPPING_SAKSBEHANDLER_REFUSJON)
@ProtectedWithClaims(issuer = "aad")
class SaksbehandlerRefusjonController(
    val innloggetBrukerService: InnloggetBrukerService,
    val auditLogger: AuditLogger
) {
    @GetMapping
    fun hentAlle(queryParametre: HentSaksbehandlerRefusjonerQueryParametre): Map<String, Any> {
        val saksbehandler = innloggetBrukerService.hentInnloggetSaksbehandler()
        val resultat = saksbehandler.finnAlle(queryParametre)
        auditLogger.logg(saksbehandler.identifikator, "Hent refusjonsliste", URI.create(REQUEST_MAPPING_ARBEIDSGIVER_REFUSJON), HttpMethod.GET, *(resultat.get("refusjoner") as List<Refusjon>).toTypedArray())
        return resultat
    }

    @GetMapping("/{id}")
    fun hent(@PathVariable id: String): Refusjon? {
        val saksbehandler = innloggetBrukerService.hentInnloggetSaksbehandler()
        val refusjon = saksbehandler.finnRefusjon(id)
        auditLogger.logg(
            saksbehandler.identifikator, "Hent detaljer om refusjon", URI.create("$REQUEST_MAPPING_ARBEIDSGIVER_REFUSJON/ID"), HttpMethod.GET, refusjon
        )
        return refusjon
    }

    @PostMapping("/{id}/forleng-frist")
    fun forlengFrist(@PathVariable id: String, @RequestBody request: ForlengFristRequest): Refusjon {
        val saksbehandler = innloggetBrukerService.hentInnloggetSaksbehandler()
        val refusjon = saksbehandler.forlengFrist(id, request.nyFrist, request.årsak)
        auditLogger.logg(
            saksbehandler.identifikator, "Forleng frist for refusjon", URI.create("$REQUEST_MAPPING_ARBEIDSGIVER_REFUSJON/ID/forleng-frist"), HttpMethod.POST, refusjon
        )
        return refusjon
    }

    @PostMapping("/{id}/merk-for-unntak-om-inntekter-to-mnd-frem")
    fun merkForUnntakOmInntekterToMånederFrem(@PathVariable id: String, @RequestBody request: MerkForUnntakOmInntekterToMånederFremRequest) {
        val saksbehandler = innloggetBrukerService.hentInnloggetSaksbehandler()
        saksbehandler.merkForUnntakOmInntekterToMånederFrem(id, request.merking)
    }
}
