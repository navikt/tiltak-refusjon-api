package no.nav.arbeidsgiver.tiltakrefusjon

import hentRefusjon
import no.nav.arbeidsgiver.tiltakrefusjon.domain.Refusjon
import no.nav.arbeidsgiver.tiltakrefusjon.domain.Refusjonsgrunnlag
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal

@RestController
@CrossOrigin(origins = ["http://localhost:3000"])

class RefusjonController {
    @GetMapping("beregn")
    fun beregn(grunnlag: Refusjonsgrunnlag): BigDecimal {
        return beregnRefusjon(grunnlag)
    }

    @GetMapping("refusjon")
    fun hent(id: String): Refusjon {
        println("Returnerer refusjon")
        return hentRefusjon("fraREpo")
    }

    @PutMapping("refusjon")
    fun lagre(refusjon: Refusjon) {
        println("Lagrer refusjon: " + refusjon)
    }
}