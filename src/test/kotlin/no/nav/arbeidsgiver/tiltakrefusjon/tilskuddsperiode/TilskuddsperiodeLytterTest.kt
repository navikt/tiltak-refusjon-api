package no.nav.arbeidsgiver.tiltakrefusjon.tilskuddsperiode

import no.nav.arbeidsgiver.tiltakrefusjon.JsonConfiguration
import no.nav.arbeidsgiver.tiltakrefusjon.medFellesOppsett
import tools.jackson.module.kotlin.jacksonMapperBuilder
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.RefusjonService
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.Tiltakstype
import no.nav.arbeidsgiver.tiltakrefusjon.utils.Now
import org.junit.jupiter.api.Test
import tools.jackson.databind.DeserializationFeature
import java.time.LocalDateTime
import java.util.*
import org.mockito.kotlin.argThat
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.junit.jupiter.api.assertThrows
import tools.jackson.core.JacksonException
import tools.jackson.databind.node.ObjectNode

class TilskuddsperiodeLytterTest {
    private val service = mock<RefusjonService>()
    private val mapper = jacksonMapperBuilder().medFellesOppsett().build()
    private val lytter = TilskuddsperiodeKafkaLytter(service, mapper)

    @Test
    fun `skal opprette refusjon når melding blir lest fra topic`() {
        val tilskuddMelding = TilskuddsperiodeGodkjentMelding.Lonnstilskudd(
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
        )

        lytter.tilskuddsperiodeGodkjent(JsonConfiguration.kafkaJsonMapper.writeValueAsString(tilskuddMelding))

        verify(service).opprettRefusjon(argThat { avtaleId == tilskuddMelding.avtaleId })
    }

    @Test
    fun `skal tåle null i satser og lønnstilskuddsprosent når FAIL_ON_NULL_FOR_PRIMITIVES er på`() {
        val strengLytter = TilskuddsperiodeKafkaLytter(
            service,
            jacksonMapperBuilder().medFellesOppsett()
                .enable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES)
                .build()
        )
        val melding = JsonConfiguration.kafkaJsonMapper.writeValueAsString(
            enLonnstilskuddMelding().copy(
                feriepengerSats = null,
                otpSats = null,
                arbeidsgiveravgiftSats = null,
                lønnstilskuddsprosent = null,
            )
        )

        strengLytter.tilskuddsperiodeGodkjent(melding)

        verify(service).opprettRefusjon(argThat {
            this is TilskuddsperiodeGodkjentMelding.Lonnstilskudd &&
                feriepengerSats == null && otpSats == null && arbeidsgiveravgiftSats == null && lønnstilskuddsprosent == null
        })
    }

    @Test
    fun `historisk melding med alle felt leses som riktig subtype`() {
        val historisk = mapper.readValue(
            JsonConfiguration.kafkaJsonMapper.writeValueAsString(enMentorMelding()),
            ObjectNode::class.java
        ).put("lønnstilskuddsprosent", 60)

        lytter.tilskuddsperiodeGodkjent(historisk.toString())

        verify(service).opprettRefusjon(argThat {
            this is TilskuddsperiodeGodkjentMelding.Mentor && mentorTimelonn == 300 && mentorAntallTimer == 10.0
        })
    }

    @Test
    fun `vtao-melding uten satser leses som Vtao`() {
        val vtao = mapper.readValue(
            JsonConfiguration.kafkaJsonMapper.writeValueAsString(enMentorMelding()),
            ObjectNode::class.java
        ).put("tiltakstype", "VTAO")
        listOf("feriepengerSats", "otpSats", "arbeidsgiveravgiftSats", "mentorTimelonn", "mentorAntallTimer").forEach { vtao.remove(it) }

        lytter.tilskuddsperiodeGodkjent(vtao.toString())

        verify(service).opprettRefusjon(argThat { this is TilskuddsperiodeGodkjentMelding.Vtao })
    }

    @Test
    fun `inkluderingstilskudd-melding leses med utgift`() {
        val inkludering = mapper.readValue(
            JsonConfiguration.kafkaJsonMapper.writeValueAsString(enMentorMelding()),
            ObjectNode::class.java
        ).put("tiltakstype", "INKLUDERINGSTILSKUDD")
        inkludering.putObject("inkluderingstilskuddsutgift")
            .put("id", "utgift-1").put("navn", "Utstyr").put("beløp", 5000).put("beskrivelse", "Hev-senk-pult").put("type", "UTSTYR")

        lytter.tilskuddsperiodeGodkjent(inkludering.toString())

        verify(service).opprettRefusjon(argThat {
            this is TilskuddsperiodeGodkjentMelding.Inkluderingstilskudd && inkluderingstilskuddsutgift?.beløp == 5000
        })
    }

    @Test
    fun `historisk inkluderingstilskudd-melding uten utgift leses`() {
        val historisk = mapper.readValue(
            JsonConfiguration.kafkaJsonMapper.writeValueAsString(enMentorMelding()),
            ObjectNode::class.java
        ).put("tiltakstype", "INKLUDERINGSTILSKUDD")

        lytter.tilskuddsperiodeGodkjent(historisk.toString())

        verify(service).opprettRefusjon(argThat {
            this is TilskuddsperiodeGodkjentMelding.Inkluderingstilskudd && inkluderingstilskuddsutgift == null
        })
    }

    @Test
    fun `melding uten tiltakstype avvises`() {
        val utenType = mapper.readValue(
            JsonConfiguration.kafkaJsonMapper.writeValueAsString(enMentorMelding()),
            ObjectNode::class.java
        )
        utenType.remove("tiltakstype")

        assertThrows<JacksonException> { lytter.tilskuddsperiodeGodkjent(utenType.toString()) }
        verifyNoInteractions(service)
    }

    private fun enLonnstilskuddMelding() = TilskuddsperiodeGodkjentMelding.Lonnstilskudd(
        avtaleId = UUID.randomUUID().toString(),
        tilskuddsperiodeId = UUID.randomUUID().toString(),
        avtaleInnholdId = UUID.randomUUID().toString(),
        tiltakstype = Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD,
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
    )

    private fun enMentorMelding() = TilskuddsperiodeGodkjentMelding.Mentor(
        avtaleId = UUID.randomUUID().toString(),
        tilskuddsperiodeId = UUID.randomUUID().toString(),
        avtaleInnholdId = UUID.randomUUID().toString(),
        tiltakstype = Tiltakstype.MENTOR,
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
        avtaleNr = 3456,
        løpenummer = 3,
        resendingsnummer = null,
        enhet = "1000",
        godkjentTidspunkt = LocalDateTime.now(),
        arbeidsgiverKontonummer = "12345678908",
        arbeidsgiverKid = null,
        mentorTimelonn = 300,
        mentorAntallTimer = 10.0,
    )
}
