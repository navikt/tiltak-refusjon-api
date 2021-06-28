package no.nav.arbeidsgiver.tiltakrefusjon.refusjon


data class RefusjonGodkjentMelding(
        val beløp: Int
      )
        {
        companion object{
                @JvmStatic
                fun create(refusjon: Refusjon): RefusjonGodkjentMelding {
                        return RefusjonGodkjentMelding(
                                refusjon.tilskuddsgrunnlag.tilskuddsbeløp
                        )
                }
        }
}