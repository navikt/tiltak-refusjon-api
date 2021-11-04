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
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.OneToOne

@Entity
data class Korreksjon(
    @ManyToOne
    @JoinColumn(name = "refusjon_id")
    private val refusjon: Refusjon,
    val korreksjonsnummer: Int,
) {
    constructor(refusjon: Refusjon, korreksjonsnummer: Int, korreksjonsgrunner: Set<Korreksjonsgrunn>) : this(
        refusjon,
        korreksjonsnummer
    ) {
        this.korreksjonsgrunner.addAll(korreksjonsgrunner)
    }

    @Id
    val id: String = ULID.random()

    @Enumerated(EnumType.STRING)
    @ElementCollection(fetch = FetchType.EAGER)
    val korreksjonsgrunner: MutableSet<Korreksjonsgrunn> = EnumSet.noneOf(Korreksjonsgrunn::class.java)

    @OneToOne(orphanRemoval = true, cascade = [CascadeType.ALL])
    var inntektsgrunnlag: Inntektsgrunnlag? = null

    @OneToOne(orphanRemoval = true, cascade = [CascadeType.ALL])
    var beregning: Beregning? = null
    @Enumerated(EnumType.STRING)
    var status: Korreksjonstype = Korreksjonstype.UTKAST

    var kostnadssted: String? = null
    var bedriftKontonummer: String? = null
    var inntekterKunFraTiltaket: Boolean? = null
    var endretBruttoLønn: Int? = null

    var godkjentTidspunkt: Instant? = null
    var godkjentAvNavIdent: String? = null
    var besluttetAvNavIdent: String? = null

    fun gjørBeregning() {
        this.beregning = beregnRefusjonsbeløp(
            inntekter = inntektsgrunnlag!!.inntekter.toList(),
            tilskuddsgrunnlag = refusjon.tilskuddsgrunnlag,
            appImageId = "",
            tidligereUtbetalt = if (korreksjonsgrunner.contains(Korreksjonsgrunn.UTBETALT_HELE_TILSKUDDSBELØP))
                refusjon.tilskuddsgrunnlag.tilskuddsbeløp
            else
                refusjon.beregning!!.refusjonsbeløp,
            korrigertBruttoLønn = endretBruttoLønn
        )
    }

    private fun krevStatus(vararg gyldigeStatuser: Korreksjonstype) {
        if (status !in gyldigeStatuser) throw FeilkodeException(Feilkode.UGYLDIG_STATUS)
    }

    // Ved positivt beløp, skal etterbetale
    fun utbetalKorreksjon(utførtAv: String, beslutterNavIdent: String, kostnadssted: String) {
        krevStatus(Korreksjonstype.UTKAST)

        val refusjonsbeløp = beregning?.refusjonsbeløp
        if (refusjonsbeløp == null || refusjonsbeløp <= 0) {
            throw FeilkodeException(Feilkode.KORREKSJONSBELOP_NEGATIVT)
        }
        if (bedriftKontonummer == null) {
            throw FeilkodeException(Feilkode.INGEN_BEDRIFTKONTONUMMER)
        }
        if (beslutterNavIdent.isNullOrBlank()) {
            throw FeilkodeException(Feilkode.INGEN_BESLUTTER)
        }
        if (beslutterNavIdent == utførtAv) {
            throw FeilkodeException(Feilkode.SAMME_SAKSBEHANDLER_OG_BESLUTTER)
        }
        this.godkjentTidspunkt = Now.instant()
        this.godkjentAvNavIdent = utførtAv
        this.besluttetAvNavIdent = beslutterNavIdent
        this.kostnadssted = kostnadssted
        this.status = Korreksjonstype.TILLEGSUTBETALING
    }

    // Ved 0 beløp, skal ikke tilbakekreve eller etterbetale
    fun fullførKorreksjonVedOppgjort(utførtAv: String) {
        val refusjonsbeløp = beregning?.refusjonsbeløp
        if (refusjonsbeløp == null || refusjonsbeløp != 0) {
            throw FeilkodeException(Feilkode.KORREKSJONSBELOP_IKKE_NULL)
        }
        this.godkjentTidspunkt = Now.instant()
        this.godkjentAvNavIdent = utførtAv
    }

    // Ved negativt beløp, skal tilbakekreves
    fun fullførKorreksjonVedTilbakekreving(utførtAv: String) {
        val refusjonsbeløp = beregning?.refusjonsbeløp
        if (refusjonsbeløp == null || refusjonsbeløp >= 0) {
            throw FeilkodeException(Feilkode.KORREKSJONSBELOP_POSITIVT)
        }
        this.godkjentTidspunkt = Now.instant()
        this.godkjentAvNavIdent = utførtAv
    }
}
