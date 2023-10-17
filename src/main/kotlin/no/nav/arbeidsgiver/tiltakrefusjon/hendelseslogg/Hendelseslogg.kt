package no.nav.arbeidsgiver.tiltakrefusjon.hendelseslogg

import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.Id
import no.nav.arbeidsgiver.tiltakrefusjon.utils.Now
import no.nav.arbeidsgiver.tiltakrefusjon.utils.ulid
import java.time.LocalDateTime

@Entity
class Hendelseslogg(
    val appImageId: String,
    val refusjonId: String,
    val korreksjonId: String?,
    val utførtAv: String,
    val event: String,
    @Convert(converter = JpaConverterMetadata::class)
    val metadata: HendelseMetadata? = null,
) {
    @Id
    val id: String = ulid()
    val tidspunkt: LocalDateTime = Now.localDateTime()
}