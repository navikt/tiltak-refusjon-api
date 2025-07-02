package no.nav.arbeidsgiver.tiltakrefusjon.rapport

import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.Refusjon
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.RefusjonStatus
import java.time.Instant

data class UbetaltFaktura(
    val refusjonsnummer: String,
    val tilskuddsperiodeId: String,
    val sendtTidspunkt: Instant?,
    val refusjonsId: String?,
    val behandlingsstatus: RefusjonStatus
) {
    companion object {
        fun fraRefusjon(refusjon: Refusjon) =
            UbetaltFaktura(
                lagId(
                    refusjon.refusjonsgrunnlag.tilskuddsgrunnlag.avtaleNr,
                    refusjon.refusjonsgrunnlag.tilskuddsgrunnlag.løpenummer,
                    null,
                    refusjon.refusjonsgrunnlag.tilskuddsgrunnlag.resendingsnummer
                ),
                refusjon.refusjonsgrunnlag.tilskuddsgrunnlag.tilskuddsperiodeId,
                refusjon.godkjentAvArbeidsgiver,
                refusjon.id,
                refusjon.status
            )
    }
}

fun lagId(avtaleNr: Int, løpenummer: Int, korreksjonsnummer: Int?, resendingsnummer: Int?): String {
    return if (korreksjonsnummer != null && resendingsnummer != null) {
        "T-${avtaleNr}-$løpenummer-K$korreksjonsnummer-R$resendingsnummer"
    } else if (korreksjonsnummer != null) {
        "T-${avtaleNr}-$løpenummer-K$korreksjonsnummer"
    } else if (resendingsnummer != null) {
        "T-${avtaleNr}-$løpenummer-R$resendingsnummer"
    } else {
        "T-${avtaleNr}-$løpenummer"
    }
}
