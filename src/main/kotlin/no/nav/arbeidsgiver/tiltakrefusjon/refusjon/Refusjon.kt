package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import com.fasterxml.jackson.annotation.JsonProperty
import com.github.guepardoapps.kulid.ULID
import no.nav.arbeidsgiver.tiltakrefusjon.Feilkode
import no.nav.arbeidsgiver.tiltakrefusjon.FeilkodeException
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.events.BeregningUtført
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.events.FristForlenget
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.events.GodkjentAvArbeidsgiver
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.events.InntekterInnhentet
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.events.KorreksjonMerketForOppgjort
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.events.KorreksjonMerketForTilbakekreving
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.events.KorreksjonSendtTilUtbetaling
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.events.RefusjonAnnullert
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.events.RefusjonForkortet
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.events.RefusjonKlar
import no.nav.arbeidsgiver.tiltakrefusjon.utils.Now
import no.nav.arbeidsgiver.tiltakrefusjon.utils.antallMånederEtter
import org.springframework.data.domain.AbstractAggregateRoot
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.util.EnumSet
import javax.persistence.CascadeType
import javax.persistence.ElementCollection
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.FetchType
import javax.persistence.Id
import javax.persistence.ManyToOne
import javax.persistence.OneToOne
import kotlin.streams.toList

@Entity
data class Refusjon(
    @ManyToOne(cascade = [CascadeType.PERSIST, CascadeType.MERGE], optional = false)
    val tilskuddsgrunnlag: Tilskuddsgrunnlag,
    val bedriftNr: String,
    val deltakerFnr: String,
    val korreksjonAvId: String? = null,
    val korreksjonsnummer: Int? = null,
) : AbstractAggregateRoot<Refusjon>() {
    @Id
    val id: String = ULID.random()

    @OneToOne(orphanRemoval = true, cascade = [CascadeType.ALL])
    var inntektsgrunnlag: Inntektsgrunnlag? = null

    @OneToOne(orphanRemoval = true, cascade = [CascadeType.ALL])
    var beregning: Beregning? = null

    // Fristen er satt til 2 mnd ihht økonomireglementet
    var fristForGodkjenning: LocalDate = antallMånederEtter(tilskuddsgrunnlag.tilskuddTom, 2)
    var forrigeFristForGodkjenning: LocalDate? = null

    var godkjentAvArbeidsgiver: Instant? = null

    var godkjentAvSaksbehandler: Instant? = null
    var godkjentAvSaksbehandlerNavIdent: String? = null
    var beslutterNavIdent: String? = null

    var bedriftKontonummer: String? = null
    var innhentetBedriftKontonummerTidspunkt: LocalDateTime? = null

    @Enumerated(EnumType.STRING)
    lateinit var status: RefusjonStatus

    var korrigeresAvId: String? = null

    @Enumerated(EnumType.STRING)
    @ElementCollection(fetch = FetchType.EAGER)
    val korreksjonsgrunner: MutableSet<Korreksjonsgrunn> = EnumSet.noneOf(Korreksjonsgrunn::class.java)

    var inntekterKunFraTiltaket: Boolean? = null
    var endretBruttoLønn: Int? = null

    init {
        oppdaterStatus()
    }

    @JsonProperty
    fun harInntektIAlleMåneder(): Boolean {
        val månederInntekter =
            inntektsgrunnlag?.inntekter?.filter { it.erMedIInntektsgrunnlag() }?.map { it.måned }?.sorted()?.distinct()
                ?: emptyList()

        val tilskuddFom = tilskuddsgrunnlag.tilskuddFom
        val tilskuddTom = tilskuddsgrunnlag.tilskuddTom

        val månederTilskudd = tilskuddFom.datesUntil(tilskuddTom).map { YearMonth.of(it.year, it.month) }.distinct().toList()

        return månederInntekter.containsAll(månederTilskudd)
    }

    private fun krevStatus(vararg gyldigeStatuser: RefusjonStatus) {
        if (status !in gyldigeStatuser) throw FeilkodeException(Feilkode.UGYLDIG_STATUS)
    }

    fun utbetalingMislykket() {
        if(status == RefusjonStatus.SENDT_KRAV || status == RefusjonStatus.UTBETALT) status = RefusjonStatus.UTBETALING_FEILET
    }

    fun utbetalingVellykket(){
        if(status == RefusjonStatus.SENDT_KRAV || status == RefusjonStatus.UTBETALING_FEILET) status = RefusjonStatus.UTBETALT
    }

    fun oppdaterStatus() {
        val statuserSomIkkeKanEndres =
            listOf(RefusjonStatus.SENDT_KRAV, RefusjonStatus.ANNULLERT, RefusjonStatus.UTBETALT)
        if (::status.isInitialized && status in statuserSomIkkeKanEndres) return

        if (korreksjonAvId != null) {
            status = RefusjonStatus.KORREKSJON_UTKAST
            return
        }

        val today = Now.localDate()
        if (today.isAfter(fristForGodkjenning)) {
            status = RefusjonStatus.UTGÅTT
            return
        }

        status = if (today.isAfter(tilskuddsgrunnlag.tilskuddTom)) {
            RefusjonStatus.KLAR_FOR_INNSENDING
        } else {
            RefusjonStatus.FOR_TIDLIG
        }
    }

    fun oppgiInntektsgrunnlag(inntektsgrunnlag: Inntektsgrunnlag) {
        oppdaterStatus()
        krevStatus(RefusjonStatus.KLAR_FOR_INNSENDING, RefusjonStatus.KORREKSJON_UTKAST)
        this.inntektsgrunnlag = inntektsgrunnlag
        registerEvent(InntekterInnhentet(this))
    }

    fun endreBruttolønn(inntekterKunFraTiltaket: Boolean, bruttoLønn: Int?) {
        oppdaterStatus()
        krevStatus(RefusjonStatus.KLAR_FOR_INNSENDING, RefusjonStatus.KORREKSJON_UTKAST)
        if (inntekterKunFraTiltaket && bruttoLønn != null) {
            throw FeilkodeException(Feilkode.INNTEKTER_KUN_FRA_TILTAK_OG_OPPGIR_BELØP)
        }
        this.inntekterKunFraTiltaket = inntekterKunFraTiltaket
        this.endretBruttoLønn = bruttoLønn
    }

    fun gjørBeregning(
        appImageId: String,
        tidligereUtbetalt: Int,
    ) {
        if (inntektsgrunnlag?.inntekter?.isNotEmpty() == true) {
            beregning = beregnRefusjonsbeløp(
                inntektsgrunnlag!!.inntekter.toList(), tilskuddsgrunnlag, appImageId,
                tidligereUtbetalt,
                endretBruttoLønn
            )
            registerEvent(BeregningUtført(this))
        }
    }

    fun godkjennForArbeidsgiver() {
        oppdaterStatus()
        krevStatus(RefusjonStatus.KLAR_FOR_INNSENDING)
        if (inntektsgrunnlag == null || inntektsgrunnlag!!.inntekter.isEmpty()) {
            throw FeilkodeException(Feilkode.INGEN_INNTEKTER)
        }
        if (bedriftKontonummer == null) {
            throw FeilkodeException(Feilkode.INGEN_BEDRIFTKONTONUMMER)
        }
        godkjentAvArbeidsgiver = Now.instant()
        status = RefusjonStatus.SENDT_KRAV
        registerEvent(GodkjentAvArbeidsgiver(this))
    }

    fun annuller() {
        oppdaterStatus()
        krevStatus(RefusjonStatus.KLAR_FOR_INNSENDING, RefusjonStatus.FOR_TIDLIG)
        status = RefusjonStatus.ANNULLERT
        registerEvent(RefusjonAnnullert(this))
    }

    fun gjørKlarTilInnsending() {
        krevStatus(RefusjonStatus.FOR_TIDLIG)
        if (Now.localDate().isAfter(tilskuddsgrunnlag.tilskuddTom)) {
            status = RefusjonStatus.KLAR_FOR_INNSENDING
            registerEvent(RefusjonKlar(this))
        }
    }

    fun forkort(tilskuddTom: LocalDate, tilskuddsbeløp: Int) {
        oppdaterStatus()
        krevStatus(RefusjonStatus.KLAR_FOR_INNSENDING, RefusjonStatus.FOR_TIDLIG)
        tilskuddsgrunnlag.tilskuddTom = tilskuddTom
        tilskuddsgrunnlag.tilskuddsbeløp = tilskuddsbeløp
        oppdaterStatus()
        registerEvent(RefusjonForkortet(this))
    }

    fun oppgiBedriftKontonummer(bedrifKontonummer: String) {
        bedriftKontonummer = bedrifKontonummer
        innhentetBedriftKontonummerTidspunkt = Now.localDateTime()
    }

    fun opprettKorreksjonsutkast(korreksjonsgrunner: Set<Korreksjonsgrunn>): Refusjon {
        krevStatus(RefusjonStatus.UTBETALT, RefusjonStatus.SENDT_KRAV, RefusjonStatus.UTGÅTT)
        if (korrigeresAvId != null) {
            throw FeilkodeException(Feilkode.HAR_KORREKSJON)
        }

        val korreksjonsnummer = if (this.korreksjonsnummer == null) 1 else this.korreksjonsnummer.plus(1)
        val korreksjon = Refusjon(this.tilskuddsgrunnlag, this.bedriftNr, this.deltakerFnr, this.id, korreksjonsnummer)

        val kopiAvInntektsgrunnlag = Inntektsgrunnlag(
            inntekter = this.inntektsgrunnlag!!.inntekter.map {
                Inntektslinje(
                    it.inntektType,
                    it.beskrivelse,
                    it.beløp,
                    it.måned,
                    it.opptjeningsperiodeFom,
                    it.opptjeningsperiodeTom
                )
            }, respons = this.inntektsgrunnlag!!.respons
        )
        kopiAvInntektsgrunnlag.innhentetTidspunkt = this.inntektsgrunnlag!!.innhentetTidspunkt
        korreksjon.inntektsgrunnlag = kopiAvInntektsgrunnlag
        korreksjon.bedriftKontonummer = this.bedriftKontonummer
        korreksjon.korreksjonsgrunner.addAll(korreksjonsgrunner)
        this.korrigeresAvId = korreksjon.id
        return korreksjon
    }

    // Ved positivt beløp, skal etterbetale
    fun utbetalKorreksjon(utførtAv: String, beslutterNavIdent: String, kostnadssted: String) {
        krevStatus(RefusjonStatus.KORREKSJON_UTKAST)
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
        status = RefusjonStatus.KORREKSJON_SENDT_TIL_UTBETALING
        godkjentAvSaksbehandler = Now.instant()
        godkjentAvSaksbehandlerNavIdent = utførtAv
        this.beslutterNavIdent = beslutterNavIdent
        val korreksjonstype =
            if (korreksjonsgrunner.contains(Korreksjonsgrunn.UTBETALING_RETURNERT)) Korreksjonstype.UTBETALING_AVVIST else Korreksjonstype.TILLEGSUTBETALING
        registerEvent(KorreksjonSendtTilUtbetaling(this, korreksjonstype))
    }

    // Ved 0 beløp, skal ikke tilbakekreve eller etterbetale
    fun fullførKorreksjonVedOppgjort(utførtAv: String) {
        krevStatus(RefusjonStatus.KORREKSJON_UTKAST)
        val refusjonsbeløp = beregning?.refusjonsbeløp
        if (refusjonsbeløp == null || refusjonsbeløp != 0) {
            throw FeilkodeException(Feilkode.KORREKSJONSBELOP_IKKE_NULL)
        }
        status = RefusjonStatus.KORREKSJON_OPPGJORT
        godkjentAvSaksbehandler = Now.instant()
        godkjentAvSaksbehandlerNavIdent = utførtAv
        registerEvent(KorreksjonMerketForOppgjort(this))
    }

    // Ved negativt beløp, skal tilbakekreves
    fun fullførKorreksjonVedTilbakekreving(utførtAv: String) {
        krevStatus(RefusjonStatus.KORREKSJON_UTKAST)
        val refusjonsbeløp = beregning?.refusjonsbeløp
        if (refusjonsbeløp == null || refusjonsbeløp >= 0) {
            throw FeilkodeException(Feilkode.KORREKSJONSBELOP_POSITIVT)
        }
        status = RefusjonStatus.KORREKSJON_SKAL_TILBAKEKREVES
        godkjentAvSaksbehandler = Now.instant()
        godkjentAvSaksbehandlerNavIdent = utførtAv
        registerEvent(KorreksjonMerketForTilbakekreving(this))
    }

    fun kanSlettes(): Boolean {
        return status == RefusjonStatus.KORREKSJON_UTKAST
    }

    fun forlengFrist(nyFrist: LocalDate, årsak: String, utførtAv: String) {
        oppdaterStatus()
        krevStatus(RefusjonStatus.FOR_TIDLIG, RefusjonStatus.KLAR_FOR_INNSENDING, RefusjonStatus.UTGÅTT)

        if (nyFrist <= fristForGodkjenning) {
            // Ny frist må være etter nåværende frist for at det skal være en forlengelse
            throw FeilkodeException(Feilkode.UGYLDIG_FORLENGELSE_AV_FRIST)
        }

        if (nyFrist > antallMånederEtter(tilskuddsgrunnlag.tilskuddTom, 6)) {
            // Kan maks forlenge 1 mnd ekstra fra opprinnelig frist på 2 mnd
            throw FeilkodeException(Feilkode.FOR_LANG_FORLENGELSE_AV_FRIST)
        }

        val gammelFristForGodkjenning = fristForGodkjenning
        forrigeFristForGodkjenning = gammelFristForGodkjenning
        fristForGodkjenning = nyFrist
        oppdaterStatus()
        registerEvent(FristForlenget(this, gammelFristForGodkjenning, fristForGodkjenning, årsak, utførtAv))
    }
}