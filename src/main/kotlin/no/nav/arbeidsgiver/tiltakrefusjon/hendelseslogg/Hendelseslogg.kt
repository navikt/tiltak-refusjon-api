package no.nav.arbeidsgiver.tiltakrefusjon.hendelseslogg

import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.Id
import no.nav.arbeidsgiver.tiltakrefusjon.utils.Now
import no.nav.arbeidsgiver.tiltakrefusjon.utils.ulid
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.LocalDateTime

@Entity
data class Hendelseslogg(
    val appImageId: String,
    val refusjonId: String,
    val korreksjonId: String?,
    val utførtAv: String,
    val event: String,
    @Convert(converter = HendelseMetadataConverter::class)
    @JdbcTypeCode(SqlTypes.JSON)
    val metadata: HendelseMetadata? = null,
) {
    @Id
    val id: String = ulid()
    val tidspunkt: LocalDateTime = Now.localDateTime()
}