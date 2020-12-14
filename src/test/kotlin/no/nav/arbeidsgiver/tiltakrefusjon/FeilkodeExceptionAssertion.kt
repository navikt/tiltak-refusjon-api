package no.nav.arbeidsgiver.tiltakrefusjon

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown

fun assertFeilkode(feilkode: Feilkode, shouldRaiseThrowable: () -> Unit) {
    try {
        shouldRaiseThrowable.invoke()
        failBecauseExceptionWasNotThrown<FeilkodeException>(FeilkodeException::class.java)
    } catch (e: Exception) {
        assertThat(e).isInstanceOf(FeilkodeException::class.java).extracting(FeilkodeException::feilkode.name).isEqualTo(feilkode)
    }
}
