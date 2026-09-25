package no.nav.arbeidsgiver.tiltakrefusjon.tilskuddsperiode

import no.nav.arbeidsgiver.tiltakrefusjon.JsonConfiguration
import no.nav.arbeidsgiver.tiltakrefusjon.medFellesOppsett
import tools.jackson.module.kotlin.jacksonMapperBuilder
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.RefusjonService
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.Tiltakstype
import no.nav.arbeidsgiver.tiltakrefusjon.utils.Now
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.*
import org.mockito.kotlin.argThat
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class TilskuddsperiodeLytterTest {
    private val service = mock<RefusjonService>()
    private val lytter = TilskuddsperiodeKafkaLytter(service, jacksonMapperBuilder().medFellesOppsett().build())

    @Test
    fun `skal opprette refusjon når melding blir lest fra topic`() {
        val tilskuddMelding = TilskuddsperiodeGodkjentMelding(
            avtaleId = UUID.randomUUID().toString(),
            tilskuddsperiodeId = UUID.randomUUID().toString(),
            avtaleInnholdId = UUID.randomUUID().toString(),
            tiltakstype = Tiltakstype.VARIG_LONNSTILSKUDD,
            deltakerFornavn = "Donald",
            deltakerEtternavn = "Duck",
            deltakerFnr = "12345678901",
            arbeidsgiverFornavn = "Arne",
            arbeidsgiverEtternavn = "Arbeidsgiver",
            arbeidsgiverTlf = "41111111",
            veilederNavIdent = "X123456",
            bedriftNavn = "Duck Levering AS",
            bedriftNr = "99999999",
            tilskuddsbeløp = 12000,
            tilskuddFom = Now.localDate().minusDays(15),
            tilskuddTom = Now.localDate(),
            feriepengerSats = 0.12,
            otpSats = 0.02,
            arbeidsgiveravgiftSats = 0.141,
            lønnstilskuddsprosent = 60,
            avtaleNr = 3456,
            løpenummer = 3,
            resendingsnummer = null,
            enhet = "1000",
            godkjentTidspunkt = LocalDateTime.now(),
            arbeidsgiverKontonummer = "12345678908",
            arbeidsgiverKid = null,
            mentorTimelonn = null,
            mentorAntallTimer = null,
        )

        lytter.tilskuddsperiodeGodkjent(JsonConfiguration.kafkaJsonMapper.writeValueAsString(tilskuddMelding))

        verify(service).opprettRefusjon(argThat { avtaleId == tilskuddMelding.avtaleId })
    }
}
