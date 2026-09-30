package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import no.nav.arbeidsgiver.tiltakrefusjon.Topics
import no.nav.arbeidsgiver.tiltakrefusjon.kafkaValueSerializer
import no.nav.arbeidsgiver.tiltakrefusjon.enRefusjon
import no.nav.arbeidsgiver.tiltakrefusjon.etTilskuddsgrunnlag
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.events.RefusjonEndretStatus
import no.nav.arbeidsgiver.tiltakrefusjon.tilskuddsperiode.TilskuddsperiodeAnnullertMelding
import no.nav.arbeidsgiver.tiltakrefusjon.utils.Now
import org.assertj.core.api.Assertions.assertThat
import org.json.JSONObject
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.kafka.core.KafkaTemplate
import java.util.concurrent.CompletableFuture

class RefusjonEndretStatusTest {
    private val refusjonEndretStatusKafkaTemplate = mock<KafkaTemplate<String, RefusjonEndretStatusMelding>>()
    private val producer = RefusjonKafkaProducer(
        mock<KafkaTemplate<String, RefusjonGodkjentMelding>>(),
        mock<KafkaTemplate<String, KorreksjonSendtTilUtbetalingMelding>>(),
        mock<KafkaTemplate<String, TilskuddsperiodeAnnullertMelding>>(),
        refusjonEndretStatusKafkaTemplate
    )

    @Test
    fun `Kafkamelding skal produseres når en refusjon endrer status`() {
        val refusjon = enRefusjon(
            etTilskuddsgrunnlag().copy(
                tilskuddFom = Now.localDate().minusMonths(1),
                tilskuddTom = Now.localDate().minusDays(1)
            )
        )
        refusjon.status = RefusjonStatus.UTGÅTT
        whenever(refusjonEndretStatusKafkaTemplate.send(any(), any(), any()))
            .thenReturn(CompletableFuture.completedFuture(mock()))

        producer.refusjonEndretStatus(RefusjonEndretStatus(refusjon))

        val melding = argumentCaptor<RefusjonEndretStatusMelding>()
        verify(refusjonEndretStatusKafkaTemplate).send(eq(Topics.REFUSJON_ENDRET_STATUS), eq(refusjon.id), melding.capture())

        val json = JSONObject(String(kafkaValueSerializer().serialize(Topics.REFUSJON_ENDRET_STATUS, melding.firstValue)!!))
        assertThat(json.keys().asSequence().toList()).containsExactlyInAnyOrder(
            "refusjonId", "bedriftNr", "avtaleId", "status", "tilskuddsperiodeId"
        )
        assertThat(json.getString("status")).isEqualTo("UTGÅTT")
        assertThat(json.getString("bedriftNr")).isEqualTo("999999999")
        assertThat(json.getString("refusjonId")).isEqualTo(refusjon.id)
        assertThat(json.getString("avtaleId")).isEqualTo(refusjon.refusjonsgrunnlag.tilskuddsgrunnlag.avtaleId)
        assertThat(json.getString("tilskuddsperiodeId"))
            .isEqualTo(refusjon.refusjonsgrunnlag.tilskuddsgrunnlag.tilskuddsperiodeId)
    }
}
