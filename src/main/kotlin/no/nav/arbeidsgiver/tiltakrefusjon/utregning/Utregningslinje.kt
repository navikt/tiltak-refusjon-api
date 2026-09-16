package no.nav.arbeidsgiver.tiltakrefusjon.utregning

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class Utregningslinje(
    val type: UtregningsradType,
    val verdi: Verdi,
    val fortegn: Fortegn? = null,
    val sats: Verdi? = null,
    var utgår: Boolean? = null
) {
    val label = type.label
    fun utgårHvis(bool: Boolean?) {
        if (bool == true) utgår = true
    }

    fun medSats(prosent: Verdi): Utregningslinje =
        this.copy(sats = prosent)
}
