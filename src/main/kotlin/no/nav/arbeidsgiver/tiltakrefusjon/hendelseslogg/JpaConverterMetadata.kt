package no.nav.arbeidsgiver.tiltakrefusjon.hendelseslogg

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

val mapper = jacksonObjectMapper()
//@Converter(autoApply = true)
class JpaConverterMetadata: AttributeConverter<HendelseMetadata, String> {

    override fun convertToDatabaseColumn(attribute: HendelseMetadata?): String? =
        if (attribute == null) null else mapper.writeValueAsString(attribute)


    override fun convertToEntityAttribute(dbData: String?): HendelseMetadata? =
        if (dbData == null) null
        else mapper.readValue(dbData, HendelseMetadata::class.java)


}