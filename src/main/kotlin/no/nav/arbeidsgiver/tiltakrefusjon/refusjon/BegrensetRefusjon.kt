package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import no.nav.team_tiltak.felles.persondata.pdl.domene.Diskresjonskode
import java.time.LocalDate

data class BegrensetRefusjon(
    val id: String,
    val korreksjonId: String?,
    val status: RefusjonStatus,
    val fristForGodkjenning: LocalDate,
    val refusjonsgrunnlag: Refusjonsgrunnlag,
    val diskresjonskode: Diskresjonskode?,
) {
    companion object {
        fun fraRefusjon(refusjon: Refusjon, diskresjonskode: Diskresjonskode?): BegrensetRefusjon {
            return BegrensetRefusjon(
                id = refusjon.id,
                korreksjonId = refusjon.korreksjonId,
                status = refusjon.status,
                fristForGodkjenning = refusjon.fristForGodkjenning,
                refusjonsgrunnlag = refusjon.refusjonsgrunnlag,
                diskresjonskode = diskresjonskode,
            )
        }
    }
}
