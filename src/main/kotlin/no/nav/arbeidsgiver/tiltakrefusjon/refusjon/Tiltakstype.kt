package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

enum class Tiltakstype {
    MIDLERTIDIG_LONNSTILSKUDD, VARIG_LONNSTILSKUDD, SOMMERJOBB, VTAO;

    fun utbetalesAutomatisk() = this == VTAO
    fun harFastUtbetalingssum() = this == VTAO
}
