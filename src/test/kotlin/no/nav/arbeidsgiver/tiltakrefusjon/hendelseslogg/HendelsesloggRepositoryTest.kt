package no.nav.arbeidsgiver.tiltakrefusjon.hendelseslogg

import jakarta.persistence.Convert
import no.nav.arbeidsgiver.tiltakrefusjon.enRefusjon
import no.nav.arbeidsgiver.tiltakrefusjon.utils.ulid
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.fail
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("local")
class HendelsesloggRepositoryTest(
    @Autowired
    val hendelsesloggRepository: HendelsesloggRepository,
) {
    @Test
    fun `finn refusjon fra tilskuddsperiodeId`() {
        val id = ulid()
        val hendelse = Hendelseslogg(
            refusjonId = id,
            event = "Hello world",
            utførtAv = "testrunner",
            korreksjonId = null,
            appImageId = ulid(),
            metadata = HendelseMetadata(antallMndFremITid=2),
        )
        hendelsesloggRepository.save(hendelse)

        val lagretHendelse =
            hendelsesloggRepository.findAll().firstOrNull { it.refusjonId == id }
                ?: fail("Fant ikke hendelse med refusjonsid $id")
        assertThat(lagretHendelse).isEqualTo(hendelse)
    }

}