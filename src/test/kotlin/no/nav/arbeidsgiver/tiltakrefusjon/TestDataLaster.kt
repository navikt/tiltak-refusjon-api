package no.nav.arbeidsgiver.tiltakrefusjon

import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.RefusjonRepository
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Profile("testdata")
@Component
class TestDataLaster(private val refusjonRepository: RefusjonRepository) : ApplicationListener<ApplicationReadyEvent> {
    override fun onApplicationEvent(event: ApplicationReadyEvent) {
        val refusjoner = refusjoner()
        println("Laster inn testdata med ${refusjoner.size} refusjoner")
        refusjonRepository.saveAll(refusjoner)
    }
}
