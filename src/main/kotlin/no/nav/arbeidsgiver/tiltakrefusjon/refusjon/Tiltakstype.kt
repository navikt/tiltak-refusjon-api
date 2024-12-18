package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

enum class Tiltakstype {
    MIDLERTIDIG_LONNSTILSKUDD, VARIG_LONNSTILSKUDD, SOMMERJOBB, VTAO;

    companion object {
        fun somUtbetalesAutomatisk() = setOf(VTAO)
    }
}
