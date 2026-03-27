package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

enum class Tiltakstype {
    MIDLERTIDIG_LONNSTILSKUDD, VARIG_LONNSTILSKUDD, SOMMERJOBB, VTAO, MENTOR, FIREARIG_LONNSTILSKUDD;

    fun utbetalesAutomatisk() = when (this) {
        MIDLERTIDIG_LONNSTILSKUDD, VARIG_LONNSTILSKUDD, SOMMERJOBB, FIREARIG_LONNSTILSKUDD -> false
        VTAO, MENTOR -> true
    }

    fun harFastUtbetalingssum() = when (this) {
        MIDLERTIDIG_LONNSTILSKUDD, VARIG_LONNSTILSKUDD, FIREARIG_LONNSTILSKUDD, SOMMERJOBB -> false
        VTAO, MENTOR -> true
    }

    fun kanIkkeOverskride5g() = when (this) {
        VARIG_LONNSTILSKUDD, FIREARIG_LONNSTILSKUDD -> true
        MIDLERTIDIG_LONNSTILSKUDD, SOMMERJOBB, VTAO, MENTOR -> false
    }
}
