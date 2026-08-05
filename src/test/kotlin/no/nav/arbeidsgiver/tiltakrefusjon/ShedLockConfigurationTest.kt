package no.nav.arbeidsgiver.tiltakrefusjon

import net.javacrumbs.shedlock.core.LockConfiguration
import net.javacrumbs.shedlock.core.LockProvider
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock
import no.nav.arbeidsgiver.tiltakrefusjon.rapport.UbetaltRefusjonRapport
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.StatusJobb
import no.nav.arbeidsgiver.tiltakrefusjon.varsling.VarslingJobb
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.test.context.ActiveProfiles
import java.time.Duration
import java.time.Instant
import java.util.UUID

@ActiveProfiles("local")
@SpringBootTest
class ShedLockConfigurationTest(
    @param:Autowired private val lockProvider: LockProvider,
) {
    @Test
    fun `jdbc lock provider forhindrer at samme jobb kjoerer samtidig`() {
        val lockConfiguration = LockConfiguration(
            Instant.now(),
            "shedlock-test-${UUID.randomUUID()}",
            Duration.ofMinutes(1),
            Duration.ZERO
        )

        val firstLock = lockProvider.lock(lockConfiguration)
        assertThat(firstLock).isPresent

        try {
            assertThat(lockProvider.lock(lockConfiguration)).isEmpty
        } finally {
            firstLock.orElseThrow().unlock()
        }
    }

    @Test
    fun `alle tidligere leader-jobber har unike scheduler-laaser`() {
        val locks = listOf(
            schedulerLock(StatusJobb::class.java, "sjekkForStatusEndring") to "PT110M",
            schedulerLock(VarslingJobb::class.java, "sjekkForRevarsling") to "PT110M",
            schedulerLock(VarslingJobb::class.java, "sjekkForVarslingKlar") to "PT2H",
            schedulerLock(UbetaltRefusjonRapport::class.java, "loggUbetalteRefusjoner") to "PT2H"
        )

        assertThat(locks.map { it.first.name }).containsExactlyInAnyOrder(
            "tiltak-refusjon-api-status-endring",
            "tiltak-refusjon-api-revarsling",
            "tiltak-refusjon-api-varsling-klar",
            "tiltak-refusjon-api-ubetalte-refusjoner"
        )
        assertThat(locks.map { it.first.name }).doesNotHaveDuplicates()
        locks.forEach { (lock, expectedLockAtMostFor) ->
            assertThat(lock.lockAtMostFor).isEqualTo(expectedLockAtMostFor)
            assertThat(lock.lockAtLeastFor).isEqualTo("PT1M")
        }
    }

    private fun schedulerLock(type: Class<*>, methodName: String): SchedulerLock {
        val method = type.getDeclaredMethod(methodName)
        assertThat(method.getAnnotation(Scheduled::class.java)).isNotNull
        return method.getAnnotation(SchedulerLock::class.java)
            ?: error("$methodName mangler @SchedulerLock")
    }
}
