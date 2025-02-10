package no.nav.arbeidsgiver.tiltakrefusjon.automatisk_utbetaling

import no.nav.arbeidsgiver.tiltakrefusjon.`Vidar Fortidlig`
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.RefusjonRepository
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.RefusjonStatus
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.StatusJobb
import no.nav.arbeidsgiver.tiltakrefusjon.utils.Now
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDate

@ActiveProfiles("local")
@SpringBootTest
class AutomatiskInnsendingServiceTest {
    @Autowired
    private lateinit var statusJobb: StatusJobb

    @Autowired
    lateinit var refusjonRepository: RefusjonRepository

    @Test
    @DirtiesContext
    fun `vtao-avtale utbetales automatisk`() {
        val vtaoRefusjon = refusjonRepository.save(`Vidar Fortidlig`())

        assertEquals(RefusjonStatus.FOR_TIDLIG, vtaoRefusjon.status)
        assertNull(vtaoRefusjon.refusjonsgrunnlag.beregning)

        statusJobb.sjekkForStatusEndring()

        val oppdatertRefusjon = refusjonRepository.findById(vtaoRefusjon.id).get()
        assertEquals(RefusjonStatus.SENDT_KRAV, oppdatertRefusjon.status)
    }

    @Test
    @DirtiesContext
    fun `vtao-avtale utbetales ikke automatisk hvis den er for tidlig`() {
        Now.fixedDate(LocalDate.now().plusMonths(6))
        val forTidligRefusjon = refusjonRepository.save(`Vidar Fortidlig`())
        Now.resetClock()
        println(forTidligRefusjon.refusjonsgrunnlag.tilskuddsgrunnlag.tilskuddFom)
        println(forTidligRefusjon.refusjonsgrunnlag.tilskuddsgrunnlag.tilskuddTom)

        statusJobb.sjekkForStatusEndring()

        val oppdatertRefusjon = refusjonRepository.findById(forTidligRefusjon.id).get()
        assertEquals(RefusjonStatus.FOR_TIDLIG, oppdatertRefusjon.status)
    }
}
