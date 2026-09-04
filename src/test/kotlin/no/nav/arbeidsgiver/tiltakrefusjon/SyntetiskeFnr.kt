package no.nav.arbeidsgiver.tiltakrefusjon

import no.bekk.bekkopen.person.FodselsnummerValidator
import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.ExtensionContext

/**
 * Skrur på syntetiske fødselsnumre for testklassen, og setter flagget tilbake etterpå.
 * Trengs bare i tester uten Spring-kontekst; RefusjonConfiguration gjør det samme for de andre.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@ExtendWith(SyntetiskeFnrExtension::class)
annotation class SyntetiskeFnr

class SyntetiskeFnrExtension : BeforeAllCallback, AfterAllCallback {
    private var opprinneligVerdi = false

    override fun beforeAll(context: ExtensionContext) {
        opprinneligVerdi = FodselsnummerValidator.ALLOW_SYNTHETIC_NUMBERS
        FodselsnummerValidator.ALLOW_SYNTHETIC_NUMBERS = true
    }

    override fun afterAll(context: ExtensionContext) {
        FodselsnummerValidator.ALLOW_SYNTHETIC_NUMBERS = opprinneligVerdi
    }
}
