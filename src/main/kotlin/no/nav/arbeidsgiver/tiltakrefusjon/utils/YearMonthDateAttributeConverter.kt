package no.nav.arbeidsgiver.tiltakrefusjon.utils

import java.time.YearMonth
import jakarta.persistence.AttributeConverter

class YearMonthDateAttributeConverter : AttributeConverter<YearMonth, String> {
    override fun convertToDatabaseColumn(attribute: YearMonth?): String? {
        return attribute?.toString()
    }

    override fun convertToEntityAttribute(dbData: String?): YearMonth? {
        return if (dbData == null) null else YearMonth.parse(dbData)
    }
}
