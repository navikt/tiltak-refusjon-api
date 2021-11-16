package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import com.github.guepardoapps.kulid.ULID
import no.nav.arbeidsgiver.tiltakrefusjon.Feilkode
import no.nav.arbeidsgiver.tiltakrefusjon.FeilkodeException
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.events.KorreksjonBeregningUtført
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.events.KorreksjonMerketForOppgjort
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.events.KorreksjonMerketForTilbakekreving
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.events.KorreksjonSendtTilUtbetaling
import no.nav.arbeidsgiver.tiltakrefusjon.utils.Now
import org.springframework.data.domain.AbstractAggregateRoot
import java.time.Instant
import java.util.*
import javax.persistence.*

@Entity
class Korreksjon(
    val korrigererRefusjonId: String,
    val korreksjonsnummer: Int,
    @OneToOne(orphanRemoval = true, cascade = [CascadeType.ALL])
    val refusjonsgrunnlag: Refusjonsgrunnlag,
    val deltakerFnr: String,
    val bedriftNr: String,
) : AbstractAggregateRoot<Korreksjon>() {
    constructor(
        korrigererRefusjonId: String,
        korreksjonsnummer: Int,
        tidligereUtbetalt: Int,
        korreksjonsgrunner: Set<Korreksjonsgrunn>,
        tilskuddsgrunnlag: Tilskuddsgrunnlag,
        deltakerFnr: String,
        bedriftNr: String,
        inntekterKunFraTiltaket: Boolean?,
        endretBruttoLønn: Int?
    ) : this(
        korrigererRefusjonId,
        korreksjonsnummer,
        Refusjonsgrunnlag(tilskuddsgrunnlag, tidligereUtbetalt),
        deltakerFnr,
        bedriftNr
    ) {
        this.korreksjonsgrunner.addAll(korreksjonsgrunner)
        if (inntekterKunFraTiltaket == null) { // For gamle refusjoner før vi stilte dette spørsmålet
            this.refusjonsgrunnlag.endreBruttolønn(true, null)
        } else {
            this.refusjonsgrunnlag.endreBruttolønn(inntekterKunFraTiltaket, endretBruttoLønn)
        }
    }

    @Id
    val id: String = ULID.random()

    @Enumerated(EnumType.STRING)
    @ElementCollection(fetch = FetchType.EAGER)
    val korreksjonsgrunner: MutableSet<Korreksjonsgrunn> = EnumSet.noneOf(Korreksjonsgrunn::class.java)

    @Enumerated(EnumType.STRING)
    var status: Korreksjonstype = Korreksjonstype.UTKAST

    var kostnadssted: String? = null

    var godkjentAvNavIdent: String? = null
    var godkjentTidspunkt: Instant? = null
    var besluttetAvNavIdent: String? = null
    var besluttetTidspunkt: Instant? = null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Korreksjon

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    private fun krevStatus(vararg gyldigeStatuser: Korreksjonstype) {
        if (status !in gyldigeStatuser) throw FeilkodeException(Feilkode.UGYLDIG_STATUS)
    }

    fun skalGjøreInntektsoppslag(): Boolean {
        if (status != Korreksjonstype.UTKAST) {
            return false
        }
        return refusjonsgrunnlag.inntektsgrunnlag?.innhentetTidspunkt?.isBefore(
            Now.localDateTime().minusMinutes(1)
        ) ?: true
    }

    // Ved positivt beløp, skal etterbetale
    fun utbetalKorreksjon(utførtAv: String, beslutterNavIdent: String, kostnadssted: String) {
        krevStatus(Korreksjonstype.UTKAST)

        val refusjonsbeløp = refusjonsgrunnlag.beregning?.refusjonsbeløp
        if (refusjonsbeløp == null || refusjonsbeløp <= 0) {
            throw FeilkodeException(Feilkode.KORREKSJONSBELOP_NEGATIVT)
        }
        if (refusjonsgrunnlag.bedriftKontonummer == null) {
            throw FeilkodeException(Feilkode.INGEN_BEDRIFTKONTONUMMER)
        }
        if (beslutterNavIdent.isBlank()) {
            throw FeilkodeException(Feilkode.INGEN_BESLUTTER)
        }
        if (beslutterNavIdent == utførtAv) {
            throw FeilkodeException(Feilkode.SAMME_SAKSBEHANDLER_OG_BESLUTTER)
        }
        if (kostnadssted.isBlank()) {
            throw FeilkodeException(Feilkode.KOSTNADSSTED_MANGLER)
        }
        this.godkjentTidspunkt = Now.instant()
        this.godkjentAvNavIdent = utførtAv
        this.besluttetAvNavIdent = beslutterNavIdent
        this.besluttetTidspunkt = Now.instant()
        this.kostnadssted = kostnadssted
        this.status = Korreksjonstype.TILLEGSUTBETALING
        registerEvent(KorreksjonSendtTilUtbetaling(this, utførtAv))
    }

    // Ved 0 beløp, skal ikke tilbakekreve eller etterbetale
    fun fullførKorreksjonVedOppgjort(utførtAv: String) {
        krevStatus(Korreksjonstype.UTKAST)
        val refusjonsbeløp = refusjonsgrunnlag.beregning?.refusjonsbeløp
        if (refusjonsbeløp == null || refusjonsbeløp != 0) {
            throw FeilkodeException(Feilkode.KORREKSJONSBELOP_IKKE_NULL)
        }
        this.godkjentTidspunkt = Now.instant()
        this.godkjentAvNavIdent = utførtAv
        this.status = Korreksjonstype.OPPGJORT
        registerEvent(KorreksjonMerketForOppgjort(this, utførtAv))
    }

    // Ved negativt beløp, skal tilbakekreves
    fun fullførKorreksjonVedTilbakekreving(utførtAv: String) {
        krevStatus(Korreksjonstype.UTKAST)
        val refusjonsbeløp = refusjonsgrunnlag.beregning?.refusjonsbeløp
        if (refusjonsbeløp == null || refusjonsbeløp >= 0) {
            throw FeilkodeException(Feilkode.KORREKSJONSBELOP_POSITIVT)
        }
        this.godkjentTidspunkt = Now.instant()
        this.godkjentAvNavIdent = utførtAv
        this.status = Korreksjonstype.TILBAKEKREVING
        registerEvent(KorreksjonMerketForTilbakekreving(this, utførtAv))
    }

    fun endreBruttolønn(inntekterKunFraTiltaket: Boolean, endretBruttoLønn: Int?) {
        krevStatus(Korreksjonstype.UTKAST)
        val harGjortBeregning = refusjonsgrunnlag.endreBruttolønn(inntekterKunFraTiltaket, endretBruttoLønn)
        if (harGjortBeregning) {
            registerEvent(KorreksjonBeregningUtført(this))
        }
    }

    fun skalGjøreKontonummerOppslag(): Boolean {
        if (status != Korreksjonstype.UTKAST) {
            return false
        }
        return refusjonsgrunnlag.bedriftKontonummerInnhentetTidspunkt?.isBefore(
            Now.localDateTime().minusMinutes(1)
        ) ?: true
    }

    fun kanSlettes(): Boolean {
        return status == Korreksjonstype.UTKAST
    }

    fun oppgiInntektsgrunnlag(inntektsgrunnlag: Inntektsgrunnlag) {
        val harGjortBeregning = this.refusjonsgrunnlag.oppgiInntektsgrunnlag(inntektsgrunnlag)
        if (harGjortBeregning) {
            registerEvent(KorreksjonBeregningUtført(this))
        }
    }

    fun oppgiBedriftKontonummer(bedrifKontonummer: String) {
        val harGjortBeregning = this.refusjonsgrunnlag.oppgiBedriftKontonummer(bedrifKontonummer)
        if (harGjortBeregning) {
            registerEvent(KorreksjonBeregningUtført(this))
        }
    }
}