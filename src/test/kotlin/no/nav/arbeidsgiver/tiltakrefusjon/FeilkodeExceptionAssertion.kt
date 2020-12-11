package no.nav.arbeidsgiver.tiltakrefusjon

import org.assertj.core.api.AbstractThrowableAssert

fun <SELF : AbstractThrowableAssert<SELF, ACTUAL>, ACTUAL : Throwable?> AbstractThrowableAssert<SELF, ACTUAL>.hasFeilkode(feilkode: Feilkode) {
    this.isInstanceOf(FeilkodeException::class.java).extracting(FeilkodeException::feilkode.name).isEqualTo(feilkode)
}
