package no.nav.arbeidsgiver.tiltakrefusjon.autorisering

import no.nav.arbeidsgiver.tiltakrefusjon.utils.Issuer
import no.nav.arbeidsgiver.tiltakrefusjon.utils.getClaims
import no.nav.security.token.support.core.context.TokenValidationContextHolder

data class AdGruppeTilganger(
    val beslutter: Boolean,
    val korreksjon: Boolean,
    val fortroligAdresse: Boolean,
    val strengtFortroligAdresse: Boolean
) {
    companion object {
        fun av(adGrupperConfig: AdGrupperConfig, context: TokenValidationContextHolder): AdGruppeTilganger {
            val groupClaim = context.getClaims(Issuer.AZURE)?.get("groups") as List<*>
            return AdGruppeTilganger(
                beslutter = groupClaim.contains(adGrupperConfig.beslutter),
                korreksjon = groupClaim.contains(adGrupperConfig.beslutter),
                fortroligAdresse = groupClaim.contains(adGrupperConfig.fortroligAdresse),
                strengtFortroligAdresse = groupClaim.contains(adGrupperConfig.strengtFortroligAdresse),
            )
        }
    }
}
