package no.nav.arbeidsgiver.tiltakrefusjon

import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.RefusjonRepository
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.Tiltakstype
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Profile("testdata")
@Component
class TestDataLaster(private val refusjonRepository: RefusjonRepository) : ApplicationListener<ApplicationReadyEvent> {
    override fun onApplicationEvent(event: ApplicationReadyEvent) {
        val refusjoner = refusjoner()
        val refusjonerMedFerietrekk = refusjonerMedFerietrekk()
        val utbetalteVarigLontilRefusjoner = gamleUtbetalteRefusjonerOgEnNy(Tiltakstype.VARIG_LONNSTILSKUDD)
        val utbetalteFireårigLontilRefusjoner = gamleUtbetalteRefusjonerOgEnNy(Tiltakstype.FIREARIG_LONNSTILSKUDD)
        println("Laster inn testdata med ${refusjoner.size + refusjonerMedFerietrekk.size + utbetalteVarigLontilRefusjoner.size + utbetalteFireårigLontilRefusjoner.size} refusjoner")
        refusjonRepository.saveAll(refusjoner)
        refusjonRepository.saveAll(refusjonerMedFerietrekk)
        refusjonRepository.saveAll(utbetalteVarigLontilRefusjoner)
        refusjonRepository.saveAll(utbetalteFireårigLontilRefusjoner)
    }
}
