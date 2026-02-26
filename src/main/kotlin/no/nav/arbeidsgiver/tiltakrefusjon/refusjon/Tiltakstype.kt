package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

enum class Tiltakstype {
    MIDLERTIDIG_LONNSTILSKUDD, VARIG_LONNSTILSKUDD, SOMMERJOBB, VTAO, MENTOR;

    fun utbetalesAutomatisk() = this == VTAO || this == MENTOR
    fun harFastUtbetalingssum() = this == VTAO || this == MENTOR
    fun kanIkkeOverskride5g() = when (this) {
        VARIG_LONNSTILSKUDD -> true
        MIDLERTIDIG_LONNSTILSKUDD, SOMMERJOBB, VTAO, MENTOR -> false
    }
}
