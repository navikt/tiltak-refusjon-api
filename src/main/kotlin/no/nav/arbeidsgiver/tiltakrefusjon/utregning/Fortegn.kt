package no.nav.arbeidsgiver.tiltakrefusjon.utregning

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

enum class Fortegn(val symbol: Char) {
    ER_LIK('='),
    PLUSS('+'),
    MINUS('-'),
    MULTIPLISER('×');

    @JsonValue
    fun jsonValue(): String = symbol.toString()

    companion object {
        @JvmStatic
        @JsonCreator
        fun fra(symbol: String): Fortegn = values().first { it.symbol.toString() == symbol }
    }
}
