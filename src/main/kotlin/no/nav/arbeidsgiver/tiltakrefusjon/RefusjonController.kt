package no.nav.arbeidsgiver.tiltakrefusjon

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class RefusjonController {
    @GetMapping("beregn")
    fun beregn(grunnlag: Refusjonsgrunnlag): Int {
        return beregnRefusjon(grunnlag)
    }
}