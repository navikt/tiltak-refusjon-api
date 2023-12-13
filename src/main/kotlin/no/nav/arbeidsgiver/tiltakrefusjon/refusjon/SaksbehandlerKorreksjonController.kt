package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import no.nav.arbeidsgiver.tiltakrefusjon.autorisering.InnloggetBrukerService
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*

const val REQUEST_MAPPING_SAKSBEHANDLER_KORREKSJON = "/api/saksbehandler/korreksjon"

@RestController
@RequestMapping(REQUEST_MAPPING_SAKSBEHANDLER_KORREKSJON)
@ProtectedWithClaims(issuer = "aad")
class SaksbehandlerKorreksjonController(
    val innloggetBrukerService: InnloggetBrukerService,
) {
    @GetMapping("/{id}")
    fun hent(@PathVariable id: String): Korreksjon? {
        val saksbehandler = innloggetBrukerService.hentInnloggetSaksbehandler()
        return saksbehandler.finnKorreksjon(id)
    }

    @PostMapping("/{id}/endre-bruttolønn")
    fun endreBruttolønn(@PathVariable id: String, @RequestBody request: EndreBruttolønnRequest) {
        val saksbehandler = innloggetBrukerService.hentInnloggetSaksbehandler()
        saksbehandler.endreBruttolønn(id, request.inntekterKunFraTiltaket, request.bruttoLønn)
    }

    @PostMapping("opprett-korreksjonsutkast")
    @Transactional
    fun opprettKorreksjonsutkast(@RequestBody request: KorrigerRequest): Refusjon {
        val saksbehandler = innloggetBrukerService.hentInnloggetSaksbehandler()
        return saksbehandler.opprettKorreksjonsutkast(request.refusjonId, request.korreksjonsgrunner, request.unntakOmInntekterFremitid, request.annenKorreksjonsGrunn)
    }

    @PostMapping("/{id}/slett-korreksjonsutkast")
    @Transactional
    fun slettKorreksjonsutkast(@PathVariable id: String) {
        val saksbehandler = innloggetBrukerService.hentInnloggetSaksbehandler()
        saksbehandler.slettKorreksjonsutkast(id)
    }

    @PostMapping("/{id}/utbetal-korreksjon")
    @Transactional
    fun utbetalKorreksjon(@PathVariable id: String) {
        val saksbehandler = innloggetBrukerService.hentInnloggetSaksbehandler()
        return saksbehandler.utbetalKorreksjon(id)
    }

    @PostMapping("/{id}/fullfør-korreksjon-ved-oppgjort")
    fun fullførKorreksjonVedOppgjort(@PathVariable id: String) {
        val saksbehandler = innloggetBrukerService.hentInnloggetSaksbehandler()
        saksbehandler.fullførKorreksjonVedOppgjort(id)
    }

    @PostMapping("/{id}/fullfør-korreksjon-ved-tilbakekreving")
    fun fullførKorreksjonVedTilbakekreving(@PathVariable id: String) {
        val saksbehandler = innloggetBrukerService.hentInnloggetSaksbehandler()
        saksbehandler.fullførKorreksjonVedTilbakekreving(id)
    }

    @PostMapping("/{id}/set-inntektslinje-opptjent-i-periode")
    fun setInntektslinjeTilOpptjentIPeriode(@PathVariable id: String, @RequestBody request: EndreRefundertInntektslinjeRequest) {
        val saksbehandler = innloggetBrukerService.hentInnloggetSaksbehandler()
        saksbehandler.setInntektslinjeTilOpptjentIPeriode(
            korreksjonId = id,
            inntekslinjeId = request.inntektslinjeId,
            erOpptjentIPeriode = request.erOpptjentIPeriode
        )
    }

    @PutMapping("/{id}/sett-manuelt-minusbeløp")
    fun settManueltMinusbeløp(@PathVariable id: String, @RequestBody minusbeløpRequest: MinusbeløpRequest) {
        val saksbehandler = innloggetBrukerService.hentInnloggetSaksbehandler()
        saksbehandler.settManueltMinusbeløp(id, minusbeløpRequest.minusbeløp)
    }

    @PostMapping("/{id}/fratrekk-sykepenger")
    @Transactional
    fun fratrekkSykepenger(@PathVariable id: String, @RequestBody request: FratrekkRefunderbarBeløp) {
        val saksbehandler = innloggetBrukerService.hentInnloggetSaksbehandler()
        saksbehandler.settFratrekkRefunderbarBeløp(id, request.fratrekkRefunderbarBeløp, request.refunderbarBeløp)
    }

    @GetMapping("/{id}/hent-enhet/{enhet}")
    fun hentEnhet(@PathVariable enhet: String): String? {
        val saksbehandler = innloggetBrukerService.hentInnloggetSaksbehandler()
        return saksbehandler.hentEnhet(enhet)
    }
}

data class MinusbeløpRequest(
    val minusbeløp: Int
)