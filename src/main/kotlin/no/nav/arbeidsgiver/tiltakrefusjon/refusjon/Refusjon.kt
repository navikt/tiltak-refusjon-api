package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import com.github.guepardoapps.kulid.ULID
import no.nav.arbeidsgiver.tiltakrefusjon.Feilkode
import no.nav.arbeidsgiver.tiltakrefusjon.FeilkodeException
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.events.GodkjentAvArbeidsgiver
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.events.GodkjentAvSaksbehandler
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.events.InntekterInnhentet
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.events.RefusjonAnnullert
import no.nav.arbeidsgiver.tiltakrefusjon.utils.Now
import org.springframework.data.domain.AbstractAggregateRoot
import java.time.Instant
import java.time.LocalDate
import javax.persistence.*

@Entity
data class Refusjon(
        @OneToOne(orphanRemoval = true, cascade = [CascadeType.ALL])
        val tilskuddsgrunnlag: Tilskuddsgrunnlag,
        val bedriftNr: String,
        val deltakerFnr: String
) : AbstractAggregateRoot<Refusjon>() {
    @Id
    val id: String = ULID.random()

    @OneToOne(orphanRemoval = true, cascade = [CascadeType.ALL])
    var inntektsgrunnlag: Inntektsgrunnlag? = null

    @OneToOne(orphanRemoval = true, cascade = [CascadeType.ALL])
    var beregning: Beregning? = null

    @Enumerated(EnumType.STRING)
    var status: RefusjonStatus = RefusjonStatus.NY

    var fristForGodkjenning: LocalDate = tilskuddsgrunnlag.tilskuddTom.plusMonths(2)

    var godkjentAvArbeidsgiver: Instant? = null
    var godkjentAvSaksbehandler: Instant? = null

    private fun krevStatus(vararg gyldigeStatuser: RefusjonStatus) {
        if (status !in gyldigeStatuser) throw FeilkodeException(Feilkode.UGYLDIG_STATUS)
    }

    fun oppgiInntektsgrunnlag(inntektsgrunnlag: Inntektsgrunnlag) {
        krevStatus(RefusjonStatus.NY, RefusjonStatus.BEREGNET)
        if (tilskuddsgrunnlag.tilskuddTom.isAfter(Now.localDate())) {
            throw FeilkodeException(Feilkode.INNTEKT_HENTET_FOR_TIDLIG)
        }
        if (fristForGodkjenning.isBefore(Now.localDate())) {
            throw FeilkodeException(Feilkode.ETTER_FRIST)
        }
        this.inntektsgrunnlag = inntektsgrunnlag
        beregning = beregnRefusjonsbeløp(inntektsgrunnlag.inntekter, tilskuddsgrunnlag)
        status = RefusjonStatus.BEREGNET
        registerEvent(InntekterInnhentet(this))
    }

    fun godkjennForArbeidsgiver() {
        krevStatus(RefusjonStatus.BEREGNET)
        if (fristForGodkjenning.isBefore(Now.localDate())) {
            throw FeilkodeException(Feilkode.ETTER_FRIST)
        }
        godkjentAvArbeidsgiver = Now.instant()
        status = RefusjonStatus.KRAV_FREMMET
        registerEvent(GodkjentAvArbeidsgiver(this))
    }

    fun godkjennForSaksbehandler() {
        krevStatus(RefusjonStatus.KRAV_FREMMET)
        godkjentAvSaksbehandler = Now.instant()
        status = RefusjonStatus.BEHANDLET
        registerEvent(GodkjentAvSaksbehandler(this))
    }

    fun annuller() {
        if (status != RefusjonStatus.NY && status != RefusjonStatus.BEREGNET) {
            println("Refusjon med id $id kan ikke annulleres. Ignorerer annullering.")
        } else {
            status = RefusjonStatus.ANNULLERT
            registerEvent(RefusjonAnnullert(this))
        }
    }
}