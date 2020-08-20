package no.nav.arbeidsgiver.tiltakrefusjon

import no.nav.arbeidsgiver.tiltakrefusjon.domain.FakeRefusjon
import no.nav.arbeidsgiver.tiltakrefusjon.domain.Refusjon
import no.nav.arbeidsgiver.tiltakrefusjon.domain.Refusjonsgrunnlag
import org.springframework.data.repository.findByIdOrNull
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal

const val REQUEST_MAPPING = "/refusjon"

@RestController
@RequestMapping(REQUEST_MAPPING)
@CrossOrigin(origins = ["http://localhost:3000"])
class RefusjonController(val refusjonRepository: RefusjonRepository) {
    @GetMapping("/beregn")
    fun beregn(grunnlag: Refusjonsgrunnlag): BigDecimal {
        return beregnRefusjon(grunnlag)
    }

    @GetMapping("/fake")
    fun hentFake(): FakeRefusjon {
        return FakeRefusjon()
    }

    @GetMapping
    fun hentAlle(): List<Refusjon> {
        return refusjonRepository.findAll()
    }

    @GetMapping("/{id}")
    fun hent(@PathVariable id: String): Refusjon? {
        return refusjonRepository.findByIdOrNull(id)
    }

    @PutMapping
    fun lagre(refusjon: Refusjon): Refusjon {
        return refusjonRepository.save(refusjon)
    }
}