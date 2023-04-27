package no.nav.arbeidsgiver.tiltakrefusjon.utils

import no.nav.arbeidsgiver.tiltakrefusjon.Feilkode
import no.nav.arbeidsgiver.tiltakrefusjon.FeilkodeException
import org.apache.commons.lang.StringUtils


/**
 * Class that represents a KID number. Contains validation logic that ensures
 * that only a valid KID can be represented by this class.
 *
 * A valid KID must either pass the mod10 check or the mod11 check.
 *
 * The class is based on Stelvio's Pid class.
 */
class KidValidator(kid: String?) {
    private val kid: String?

    /**
     * Constructs a new Kid.
     *
     * @param kid
     * The KID number.
     */
    init {
        this.kid = StringUtils.deleteWhitespace(kid)
        validate()
    }

    /**
     * If the KID number is not valid an exception is thrown.
     */
    private fun validate() {
        if (!isValidKid) {
            throw FeilkodeException(Feilkode.FEIL_BEDRIFT_KID_NUMMER)
        }
    }

    private val isValidKid: Boolean
        /**
         * Performs several validity checks.
         *
         * @return true if KID is valid, false otherwise.
         */
        private get() {
            if (kid != null) {
                if (isValidCharacters) {
                    if (isMod10Compliant || isMod11Compliant) {
                        return true
                    }
                }
            }
            return false
        }
    private val isValidCharacters: Boolean
        /**
         * Checks if the KID contains only numbers, or only numbers and a '-' at the
         * end.
         *
         * @return true if KID contains valid characters, false otherwise.
         */
        private get() = if (kidEndsWithDash()) {
            StringUtils.isNumeric(kid!!.substring(0, kid.length - 1))
        } else {
            StringUtils.isNumeric(kid)
        }

    /**
     * Checks if the KID ends with a '-'.
     *
     * @return true if the KID ends with '-', false otherwise.
     */
    private fun kidEndsWithDash(): Boolean {
        return kid!![kid.length - 1] == '-'
    }

    private val isMod10Compliant: Boolean
        /**
         * Performs mod10 validation on the KID number.
         *
         * @return true if mod10 validation is successful, false otherwise.
         */
        private get() {
            if (kidEndsWithDash()) {
                return false
            }
            val controlDigit = kid!!.substring(kid.length - 1, kid.length).toInt()
            val number = kid.substring(0, kid.length - 1)
            var sum = 0
            var alternate = true
            for (i in number.length - 1 downTo 0) {
                var n = number.substring(i, i + 1).toInt()
                if (alternate) {
                    n *= 2
                    if (n > 9) {
                        n = n % 10 + 1
                    }
                }
                sum += n
                alternate = !alternate
            }
            val remainder = sum % 10
            val calculatedControlDigit = if (remainder == 0) 0 else 10 - remainder
            return calculatedControlDigit == controlDigit
        }
    private val isMod11Compliant: Boolean
        /**
         * Performs mod11 validation on the KID number.
         *
         * @return true if mod11 validation is successful, false otherwise.
         */
        private get() {
            val number = kid!!.substring(0, kid.length - 1)
            var sum = 0
            val mod11Factor = Mod11Factor()
            for (i in number.length - 1 downTo 0) {
                var n = number.substring(i, i + 1).toInt()
                n *= mod11Factor.nextFactor
                sum += n
            }
            val remainder = sum % 11
            val calculatedControlDigit = if (remainder == 0) 0 else 11 - remainder
            return if (kidEndsWithDash()) {
                calculatedControlDigit == 10
            } else {
                val controlDigit = kid.substring(kid.length - 1, kid.length).toInt()
                calculatedControlDigit == controlDigit
            }
        }

    /**
     * Encapsulates the factors used for mod11 validation.
     * The factors are 2, 3, 4, 5, 6, 7, 2, 3, 4 etc.
     */
    private inner class Mod11Factor
    /**
     * Constructs a new Mod11Factor and sets the startvalue of factor.
     */
    {
        private var factor = 2

        val nextFactor: Int
            /**
             * Gets the next factor.
             *
             * @return The next factor.
             */
            get() {
                if (factor > 7) {
                    factor = 2
                }
                return factor++
            }
    }
}