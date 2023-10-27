package no.nav.arbeidsgiver.tiltakrefusjon.hendelseslogg

import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.events.SaksbehandlerMerketForInntekterLengerFrem
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.events.SporbarKorreksjonHendelse
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.events.SporbarRefusjonHendelse
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.events.SporbarRefusjonVarsel
import no.nav.arbeidsgiver.tiltakrefusjon.varsling.VarselType
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class EndretGrunnlagLytter(
    val hendelsesloggRepository: HendelsesloggRepository,
    @Value("\${NAIS_APP_IMAGE:}")
    val appImageId: String,
) {
    @EventListener
    fun sporbarHendelse(event: SporbarRefusjonHendelse) {
        val hendelse: Hendelseslogg
        if (event is SaksbehandlerMerketForInntekterLengerFrem) {
            hendelse = Hendelseslogg(
                appImageId = appImageId,
                refusjonId = event.refusjon.id,
                korreksjonId = null,
                utførtAv = event.utførtAv.identifikator,
                utførtRolle = event.utførtAv.rolle,
                event = event.javaClass.simpleName,
                metadata = HendelseMetadata(
                    antallMndFremITid = event.merking,
                )
            )
        } else {
            hendelse = Hendelseslogg(
                appImageId = appImageId,
                refusjonId = event.refusjon.id,
                korreksjonId = null,
                utførtAv = event.utførtAv.identifikator,
                utførtRolle = event.utførtAv.rolle,
                event = event.javaClass.simpleName,
            )
        }
        hendelsesloggRepository.save(hendelse)
    }

    @EventListener
    fun sporbarHendelse(event: SporbarKorreksjonHendelse) {
        val hendelse = Hendelseslogg(
            appImageId = appImageId,
            refusjonId = event.korreksjon.korrigererRefusjonId,
            korreksjonId = event.korreksjon.id,
            utførtAv = event.utførtAv.identifikator,
            utførtRolle = event.utførtAv.rolle,
            event = event.javaClass.simpleName,
        )
        hendelsesloggRepository.save(hendelse)
    }

    @EventListener
    fun sporbarHendelse(event: SporbarRefusjonVarsel) {
        val hendelse = Hendelseslogg(
            appImageId = appImageId,
            refusjonId = event.refusjonId,
            korreksjonId = null,
            utførtAv = event.utførtAv.identifikator,
            utførtRolle = event.utførtAv.rolle,
            event = when (event.varselType) {
                VarselType.KLAR -> "RefusjonVarselKlar"
                VarselType.REVARSEL -> "RefusjonVarselRevarsel"
                VarselType.FRIST_FORLENGET -> "RefusjonVarselFristForlenget"
                VarselType.KORRIGERT -> "RefusjonVarselKorrigert"
            }
        )
        hendelsesloggRepository.save(hendelse)
    }
}