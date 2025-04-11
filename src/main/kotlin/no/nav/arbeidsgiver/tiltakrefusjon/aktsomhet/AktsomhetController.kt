package no.nav.arbeidsgiver.tiltakrefusjon.aktsomhet

import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
class AktsomhetController(
    val aktsomhetService: AktsomhetService,
) {
    @GetMapping("/arbeidsgiver/refusjon/{id}/aktsomhet")
    @ProtectedWithClaims(issuer = "tokenx")
    fun aktsomhetArbeidsgiver(@PathVariable id: String): Aktsomhet {
        return aktsomhetService.kreverAktsomhet(id)
    }

    @GetMapping("/saksbehandler/refusjon/{id}/aktsomhet")
    @ProtectedWithClaims(issuer = "aad")
    fun aktsomhetSaksbehandler(@PathVariable id: String): Aktsomhet {
        return aktsomhetService.kreverAktsomhet(id)
    }

}
