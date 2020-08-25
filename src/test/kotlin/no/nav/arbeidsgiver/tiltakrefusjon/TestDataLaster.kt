package no.nav.arbeidsgiver.tiltakrefusjon

import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component

@Component
class TestDataLaster(private val refusjonRepository: RefusjonRepository) : ApplicationListener<ApplicationReadyEvent> {
    override fun onApplicationEvent(event: ApplicationReadyEvent) {
        refusjonRepository.saveAll(toRefusjoner())

    }
}