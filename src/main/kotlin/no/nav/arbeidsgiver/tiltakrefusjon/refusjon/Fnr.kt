package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import no.bekk.bekkopen.person.FodselsnummerCalculator
import no.bekk.bekkopen.person.FodselsnummerValidator
import org.apache.commons.lang3.NotImplementedException
import java.time.LocalDate
import java.time.ZoneId
import java.util.*

data class Fnr(val verdi: String) {
    init {
        if (!erGyldigFnr(verdi)) {
            throw FnrException()
        }
    }

    companion object {
        fun generer(aar: Int, maned: Int, dag: Int): Fnr {
            return generer(LocalDate.of(aar, maned, dag))
        }

        fun generer(dato: LocalDate): Fnr {
            if (!FodselsnummerValidator.ALLOW_SYNTHETIC_NUMBERS) {
                throw NotImplementedException("Generering av syntetiske fødselsnumre er ikke tillatt i produksjon.")
            }
            val date = Date.from(dato.atStartOfDay(ZoneId.systemDefault()).toInstant())
            val fnr = FodselsnummerCalculator.getFodselsnummerForDate(date)
            return Fnr(fnr.getValue())
        }
    }
}

class FnrException : IllegalArgumentException("Ugyldig fødselsnummer")

fun erGyldigFnr(fnr: String): Boolean {
    if (FodselsnummerValidator.ALLOW_SYNTHETIC_NUMBERS) {
        return when (fnr) {
            "15000000000", "12345678910", "12345678901", "00000000000", "11111111111", "99999999999", "07049223188", "07049223190" -> true
            else -> FodselsnummerValidator.isValid(fnr)
        }
    }
    return FodselsnummerValidator.isValid(fnr)
}
