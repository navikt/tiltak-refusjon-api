package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import com.github.guepardoapps.kulid.ULID
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.events.GodkjentAvArbeidsgiver
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.events.GodkjentAvSaksbehandler
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.events.InntekterInnhentet
import org.springframework.data.domain.AbstractAggregateRoot
import java.time.Instant
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
    var status: RefusjonStatus = RefusjonStatus.UBEHANDLET
    var godkjentAvArbeidsgiver: Instant? = null
    var godkjentAvSaksbehandler: Instant? = null

    fun oppgiInntektsgrunnlag(inntektsgrunnlag: Inntektsgrunnlag) {
        this.inntektsgrunnlag = inntektsgrunnlag
        beregning = beregnRefusjonsbeløp(inntektsgrunnlag.inntekter, tilskuddsgrunnlag)
        registerEvent(InntekterInnhentet(this))
    }

    fun godkjennForArbeidsgiver() {
        if (beregning == null) {
            throw FeilkodeException(Feilkode.MANGLER_BEREGNING)
        }
        if (godkjentAvArbeidsgiver != null) {
            throw FeilkodeException(Feilkode.KAN_IKKE_GODKJENNE_FLERE_GANGER)
        }
        godkjentAvArbeidsgiver = Instant.now()
        registerEvent(GodkjentAvArbeidsgiver(this))
    }

    fun godkjennForSaksbehandler() {
        if (godkjentAvArbeidsgiver == null) {
            throw FeilkodeException(Feilkode.MANGLER_ARBEIDSGIVERS_GODKJENNING)
        }
        if (godkjentAvSaksbehandler != null) {
            throw FeilkodeException(Feilkode.KAN_IKKE_GODKJENNE_FLERE_GANGER)
        }
        godkjentAvSaksbehandler = Instant.now()
        registerEvent(GodkjentAvSaksbehandler(this))
    }
}