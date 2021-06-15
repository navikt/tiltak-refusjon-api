package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import com.github.guepardoapps.kulid.ULID
import no.nav.arbeidsgiver.tiltakrefusjon.Feilkode
import no.nav.arbeidsgiver.tiltakrefusjon.FeilkodeException
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.events.GodkjentAvArbeidsgiver
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
    val deltakerFnr: String,

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
        krevStatus(RefusjonStatus.NY)
        if (tilskuddsgrunnlag.tilskuddTom.isAfter(Now.localDate())) {
            throw FeilkodeException(Feilkode.INNTEKT_HENTET_FOR_TIDLIG)
        }
        if (inntektsgrunnlag.inntekter.isEmpty()) {
            throw FeilkodeException(Feilkode.INGEN_INNTEKTER)
        }
        if (fristForGodkjenning.isBefore(Now.localDate())) {
            status = RefusjonStatus.UTGÅTT
        } else {
            beregning = beregnRefusjonsbeløp(inntektsgrunnlag.inntekter, tilskuddsgrunnlag)
            this.inntektsgrunnlag = inntektsgrunnlag
            registerEvent(InntekterInnhentet(this))
        }
    }

    fun godkjennForArbeidsgiver() {
        krevStatus(RefusjonStatus.NY)
        if (fristForGodkjenning.isBefore(Now.localDate())) {
            throw FeilkodeException(Feilkode.ETTER_FRIST)
        }
        if (inntektsgrunnlag == null || inntektsgrunnlag!!.inntekter.isEmpty()) {
            throw FeilkodeException(Feilkode.INGEN_INNTEKTER)
        }
        godkjentAvArbeidsgiver = Now.instant()
        status = RefusjonStatus.SENDT_KRAV
        registerEvent(GodkjentAvArbeidsgiver(this))
    }

    fun annuller() {
        if (status != RefusjonStatus.NY) {
            println("Refusjon med id $id kan ikke annulleres. Ignorerer annullering.")
        } else {
            status = RefusjonStatus.ANNULLERT
            registerEvent(RefusjonAnnullert(this))
        }
    }

    fun forkort(tilskuddTom: LocalDate, tilskuddsbeløp: Int) {
        if (status != RefusjonStatus.NY) {
            println("Refusjon med id $id kan ikke forkortes. Ignorerer forkorting.")
        } else {
            tilskuddsgrunnlag.tilskuddTom = tilskuddTom
            tilskuddsgrunnlag.tilskuddsbeløp = tilskuddsbeløp
            registerEvent(RefusjonAnnullert(this))
        }
    }
}