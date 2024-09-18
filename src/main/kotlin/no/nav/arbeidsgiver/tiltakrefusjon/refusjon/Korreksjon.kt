package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.persistence.CascadeType
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.OneToOne
import no.nav.arbeidsgiver.tiltakrefusjon.Feilkode
import no.nav.arbeidsgiver.tiltakrefusjon.FeilkodeException
import no.nav.arbeidsgiver.tiltakrefusjon.audit.FnrOgBedrift
import no.nav.arbeidsgiver.tiltakrefusjon.audit.RefusjonMedFnrOgBedrift
import no.nav.arbeidsgiver.tiltakrefusjon.autorisering.InnloggetBruker
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.events.KorreksjonMerketForOppgjort
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.events.KorreksjonMerketForTilbakekreving
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.events.KorreksjonSendtTilUtbetaling
import no.nav.arbeidsgiver.tiltakrefusjon.utils.Now
import no.nav.arbeidsgiver.tiltakrefusjon.utils.ulid
import org.springframework.data.domain.AbstractAggregateRoot
import java.time.Instant
import java.util.*

@Entity
class Korreksjon(
    val korrigererRefusjonId: String,
    val korreksjonsnummer: Int,
    @OneToOne(orphanRemoval = true, cascade = [CascadeType.ALL])
    val refusjonsgrunnlag: Refusjonsgrunnlag,
    val deltakerFnr: String,
    val bedriftNr: String,
    val unntakOmInntekterFremitid: Int?,
    val annenGrunn: String?
) : AbstractAggregateRoot<Korreksjon>(), RefusjonMedFnrOgBedrift {
    constructor(
        korrigererRefusjonId: String,
        korreksjonsnummer: Int,
        tidligereUtbetalt: Int,
        korreksjonsgrunner: Set<Korreksjonsgrunn>,
        tilskuddsgrunnlag: Tilskuddsgrunnlag,
        deltakerFnr: String,
        bedriftNr: String,
        inntekterKunFraTiltaket: Boolean?,
        endretBruttoLønn: Int?,
        unntakOmInntekterFremitid: Int?,
        annenGrunn: String?
    ) : this(
        korrigererRefusjonId,
        korreksjonsnummer,
        Refusjonsgrunnlag(tilskuddsgrunnlag, tidligereUtbetalt),
        deltakerFnr,
        bedriftNr,
        unntakOmInntekterFremitid,
        annenGrunn
    ) {
        this.korreksjonsgrunner.addAll(korreksjonsgrunner)
        if (inntekterKunFraTiltaket == null) { // For gamle refusjoner før vi stilte dette spørsmålet
            this.refusjonsgrunnlag.endreBruttolønn(true, null)
        } else {
            this.refusjonsgrunnlag.endreBruttolønn(inntekterKunFraTiltaket, endretBruttoLønn)
        }
    }

    @Id
    val id: String = ulid()

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

    @JsonProperty
    fun harTattStillingTilAlleInntektslinjer(): Boolean = if (refusjonsgrunnlag.tilskuddsgrunnlag.tiltakstype == Tiltakstype.VTAO) true else (refusjonsgrunnlag.inntektsgrunnlag?.inntekter?.filter { it.erMedIInntektsgrunnlag() }?.find { inntekt -> inntekt.erOpptjentIPeriode === null } === null)

    override fun getFnrOgBedrift(): FnrOgBedrift = FnrOgBedrift(deltakerFnr, bedriftNr)

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
    fun utbetalKorreksjon(utførtAv: InnloggetBruker, kostnadssted: String) {
        krevStatus(Korreksjonstype.UTKAST)

        val refusjonsbeløp = refusjonsgrunnlag.beregning?.refusjonsbeløp
        if (refusjonsbeløp == null || refusjonsbeløp <= 0) {
            throw FeilkodeException(Feilkode.KORREKSJONSBELOP_NEGATIVT)
        }
        if (refusjonsgrunnlag.bedriftKontonummer == null) {
            throw FeilkodeException(Feilkode.INGEN_BEDRIFTKONTONUMMER)
        }
        if (utførtAv.identifikator.isBlank()) {
            throw FeilkodeException(Feilkode.INGEN_BESLUTTER)
        }
        if (kostnadssted.isBlank()) {
            throw FeilkodeException(Feilkode.KOSTNADSSTED_MANGLER)
        }
        if (!this.harTattStillingTilAlleInntektslinjer()) {
            throw FeilkodeException(Feilkode.IKKE_TATT_STILLING_TIL_ALLE_INNTEKTSLINJER)
        }

        this.godkjentTidspunkt = Now.instant()
        this.godkjentAvNavIdent = utførtAv.identifikator
        this.besluttetAvNavIdent = utførtAv.identifikator
        this.besluttetTidspunkt = Now.instant()
        this.kostnadssted = kostnadssted
        this.status = Korreksjonstype.TILLEGSUTBETALING
        registerEvent(KorreksjonSendtTilUtbetaling(this, utførtAv))
    }

    // Ved 0 beløp, skal ikke tilbakekreve eller etterbetale
    fun fullførKorreksjonVedOppgjort(utførtAv: InnloggetBruker) {
        krevStatus(Korreksjonstype.UTKAST)
        val refusjonsbeløp = refusjonsgrunnlag.beregning?.refusjonsbeløp
        if (refusjonsbeløp == null || refusjonsbeløp != 0) {
            throw FeilkodeException(Feilkode.KORREKSJONSBELOP_IKKE_NULL)
        }
        if (!this.harTattStillingTilAlleInntektslinjer()) {
            throw FeilkodeException(Feilkode.IKKE_TATT_STILLING_TIL_ALLE_INNTEKTSLINJER)
        }
        this.godkjentTidspunkt = Now.instant()
        this.godkjentAvNavIdent = utførtAv.identifikator
        this.status = Korreksjonstype.OPPGJORT
        registerEvent(KorreksjonMerketForOppgjort(this, utførtAv))
    }

    // Ved negativt beløp, skal tilbakekreves
    fun fullførKorreksjonVedTilbakekreving(utførtAv: InnloggetBruker) {
        krevStatus(Korreksjonstype.UTKAST)
        val refusjonsbeløp = refusjonsgrunnlag.beregning?.refusjonsbeløp
        if (refusjonsbeløp == null || refusjonsbeløp >= 0) {
            throw FeilkodeException(Feilkode.KORREKSJONSBELOP_POSITIVT)
        }
        if (!this.harTattStillingTilAlleInntektslinjer()) {
            throw FeilkodeException(Feilkode.IKKE_TATT_STILLING_TIL_ALLE_INNTEKTSLINJER)
        }
        this.godkjentTidspunkt = Now.instant()
        this.godkjentAvNavIdent = utførtAv.identifikator
        this.status = Korreksjonstype.TILBAKEKREVING
        registerEvent(KorreksjonMerketForTilbakekreving(this, utførtAv))
    }

    fun endreBruttolønn(inntekterKunFraTiltaket: Boolean?, endretBruttoLønn: Int?) {
        krevStatus(Korreksjonstype.UTKAST)
        refusjonsgrunnlag.endreBruttolønn(inntekterKunFraTiltaket, endretBruttoLønn)
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
        this.refusjonsgrunnlag.oppgiInntektsgrunnlag(inntektsgrunnlag, this.refusjonsgrunnlag.inntektsgrunnlag)
    }

    fun oppgiBedriftKontonummer(bedrifKontonummer: String) {
        this.refusjonsgrunnlag.oppgiBedriftKontonummer(bedrifKontonummer)
    }

    fun setInntektslinjeTilOpptjentIPeriode(inntekslinjeId: String, erOpptjentIPeriode: Boolean) {
        krevStatus(Korreksjonstype.UTKAST)
        refusjonsgrunnlag.setInntektslinjeTilOpptjentIPeriode(inntekslinjeId, erOpptjentIPeriode)
    }

    fun settFratrekkRefunderbarBeløp(fratrekkRefunderbarBeløp: Boolean, refunderbarBeløp: Int?) {
        krevStatus(Korreksjonstype.UTKAST)
        refusjonsgrunnlag.settFratrekkRefunderbarBeløp(fratrekkRefunderbarBeløp, refunderbarBeløp)
    }

    fun utbetalingMislykket() {
        if (status == Korreksjonstype.TILLEGSUTBETALING) {
            status = Korreksjonstype.TILLEGGSUTBETALING_FEILET
        }
    }

    fun utbetalingVellykket() {
        if (status == Korreksjonstype.TILLEGSUTBETALING || status == Korreksjonstype.TILLEGGSUTBETALING_FEILET) {
            status = Korreksjonstype.TILLEGGSUTBETALING_UTBETALT
        }
    }

}
