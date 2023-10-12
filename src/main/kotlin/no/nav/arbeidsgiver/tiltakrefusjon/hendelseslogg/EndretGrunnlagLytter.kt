package no.nav.arbeidsgiver.tiltakrefusjon.hendelseslogg

import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.events.SporbarKorreksjonHendelse
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.events.SporbarRefusjonHendelse
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.events.SporbarRefusjonVarsel
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
        val hendelse = Hendelseslogg(
            appImageId = appImageId,
            refusjonId = event.refusjon.id,
            korreksjonId = null,
            utførtAv = event.utførtAv,
            event = event.javaClass.simpleName,
        )
        hendelsesloggRepository.save(hendelse)
    }

    @EventListener
    fun sporbarHendelse(event: SporbarKorreksjonHendelse) {
        val hendelse = Hendelseslogg(
            appImageId = appImageId,
            refusjonId = event.korreksjon.korrigererRefusjonId,
            korreksjonId = event.korreksjon.id,
            utførtAv = event.utførtAv,
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
            utførtAv = event.utførtAv,
            event = event.varselType.name,
        )
        hendelsesloggRepository.save(hendelse)
    }
}