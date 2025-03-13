package no.nav.arbeidsgiver.tiltakrefusjon.autorisering

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import java.io.IOException

enum class Diskresjonskode {
    STRENGT_FORTROLIG_UTLAND,
    STRENGT_FORTROLIG,
    FORTROLIG,
    UGRADERT;

    fun erKode6(): Boolean {
        return STRENGT_FORTROLIG == this || STRENGT_FORTROLIG_UTLAND == this
    }

    fun erKode7(): Boolean {
        return FORTROLIG == this
    }

    fun erKode6Eller7(): Boolean {
        return erKode6() || erKode7()
    }

    class DiskresjonskodeDeserializer : JsonDeserializer<Diskresjonskode>() {
        @Throws(IOException::class)
        override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Diskresjonskode {
            return Diskresjonskode.parse(p.valueAsString)
        }
    }

    companion object {
        fun parse(str: String?): Diskresjonskode {
            return when (str) {
                "STRENGT_FORTROLIG_UTLAND" -> STRENGT_FORTROLIG_UTLAND
                "STRENGT_FORTROLIG" -> STRENGT_FORTROLIG
                "FORTROLIG" -> FORTROLIG
                else -> UGRADERT
            }
        }
    }
}
