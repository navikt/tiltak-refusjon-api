package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import com.github.guepardoapps.kulid.ULID
import no.nav.arbeidsgiver.tiltakrefusjon.Feilkode
import no.nav.arbeidsgiver.tiltakrefusjon.FeilkodeException
import no.nav.arbeidsgiver.tiltakrefusjon.utils.Now
import java.time.Instant
import java.util.EnumSet
import javax.persistence.CascadeType
import javax.persistence.ElementCollection
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.FetchType
import javax.persistence.Id
import javax.persistence.OneToOne

@Entity
class Korreksjon(
    val korrigererRefusjonId: String,
    val korreksjonsnummer: Int,
    @OneToOne(orphanRemoval = true, cascade = [CascadeType.ALL])
    val refusjonsgrunnlag: Refusjonsgrunnlag,
    val deltakerFnr: String,
    val bedriftNr: String,
) {
    constructor(
        refusjon: String,
        korreksjonsnummer: Int,
        tidligereUtbetalt: Int,
        korreksjonsgrunner: Set<Korreksjonsgrunn>,
        tilskuddsgrunnlag: Tilskuddsgrunnlag,
        deltakerFnr: String,
        bedriftNr: String,
        inntekterKunFraTiltaket: Boolean,
        endretBruttoLønn: Int?
    ) : this(
        refusjon,
        korreksjonsnummer,
        Refusjonsgrunnlag(tilskuddsgrunnlag, tidligereUtbetalt),
        deltakerFnr,
        bedriftNr
    ) {
        this.korreksjonsgrunner.addAll(korreksjonsgrunner)
        this.refusjonsgrunnlag.endreBruttolønn(inntekterKunFraTiltaket, endretBruttoLønn)
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
        this.godkjentTidspunkt = Now.instant()
        this.godkjentAvNavIdent = utførtAv
        this.besluttetAvNavIdent = beslutterNavIdent
        this.besluttetTidspunkt = Now.instant()
        this.kostnadssted = kostnadssted
        this.status = Korreksjonstype.TILLEGSUTBETALING
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
    }

    fun endreBruttolønn(inntekterKunFraTiltaket: Boolean, endretBruttoLønn: Int?) {
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
}
