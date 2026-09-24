package no.nav.arbeidsgiver.tiltakrefusjon

import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.RefusjonGodkjentMelding
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.Tiltakstype
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.events.AuditEntry
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.events.EventType
import no.nav.arbeidsgiver.tiltakrefusjon.tilskuddsperiode.MidlerFrigjortÅrsak
import no.nav.arbeidsgiver.tiltakrefusjon.tilskuddsperiode.TilskuddsperiodeAnnullertMelding
import org.apache.kafka.common.serialization.Serializer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import tools.jackson.databind.json.JsonMapper
import java.net.URI
import java.time.Instant
import java.time.LocalDate

/** Value-serializeren Spring Boot setter på Kafka-produsentene via JsonConfiguration. */
fun kafkaValueSerializer(): Serializer<Any> =
    DefaultKafkaProducerFactory<Any, Any>(emptyMap())
        .also { JsonConfiguration().kafkaJsonSerializerCustomizer().customize(it) }
        .valueSerializerSupplier!!.get()!!

class JsonConfigurationTest {

    private val restMapper: JsonMapper = JsonMapper.builder()
        .also { JsonConfiguration().tiltakRefusjonJsonMapperCustomizer().customize(it) }
        .build()

    private fun kafkaJson(melding: Any) =
        restMapper.readTree(kafkaValueSerializer().serialize("topic", melding))

    @Test
    fun `REST-mapper beholder egenskaper som starter med æ, ø eller å`() {
        val melding = TilskuddsperiodeAnnullertMelding("id", MidlerFrigjortÅrsak.REFUSJON_FRIST_UTGÅTT)

        val json = restMapper.writeValueAsString(melding)

        assertThat(restMapper.readTree(json)["årsak"].asString()).isEqualTo("REFUSJON_FRIST_UTGÅTT")
        assertThat(restMapper.readValue(json, TilskuddsperiodeAnnullertMelding::class.java)).isEqualTo(melding)
    }

    @Test
    fun `REST-mapper skriver datoer som ISO-strenger`() {
        val json = restMapper.readTree(restMapper.writeValueAsString(mapOf("dato" to LocalDate.of(2026, 8, 1))))

        assertThat(json["dato"].asString()).isEqualTo("2026-08-01")
    }

    @Test
    fun `Kafka-serializer beholder egenskaper som starter med æ, ø eller å`() {
        val melding = TilskuddsperiodeAnnullertMelding("id", MidlerFrigjortÅrsak.AVTALE_ANNULLERT)

        val json = kafkaJson(melding)

        assertThat(json["årsak"].asString()).isEqualTo("AVTALE_ANNULLERT")
    }

    @Test
    fun `Kafka-serializer skriver LocalDate som tall-array slik konsumentene forventer`() {
        val refusjon = gamleUtbetalteRefusjonerOgEnNy(Tiltakstype.MIDLERTIDIG_LONNSTILSKUDD).first()
        val melding = RefusjonGodkjentMelding.create(refusjon)
        val fom = melding.tilskuddFom

        val json = kafkaJson(melding)

        assertThat(json["tilskuddFom"].isArray).isTrue()
        assertThat(json["tilskuddFom"].values().map { it.asInt() })
            .containsExactly(fom.year, fom.monthValue, fom.dayOfMonth)
        assertThat(json["løpenummer"].asInt()).isEqualTo(melding.løpenummer)
    }

    @Test
    fun `Kafka-serializer skriver Instant som epoch-sekunder`() {
        val tidspunkt = Instant.parse("2026-01-02T03:04:05.123Z")
        val entry = AuditEntry(
            "app", "X123456", "999999999", "id", EventType.READ, true, tidspunkt,
            "beskrivelse", URI("http://localhost/api"), "GET", "correlation-id"
        )

        val json = kafkaJson(entry)

        assertThat(json["oppslagUtførtTid"].decimalValue()).isEqualByComparingTo("1767323045.123")
        assertThat(json["forespørselTillatt"].asBoolean()).isTrue()
    }
}
