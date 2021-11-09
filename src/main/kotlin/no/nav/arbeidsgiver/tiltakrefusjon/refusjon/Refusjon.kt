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
class Refusjon(
    @OneToOne(orphanRemoval = true, cascade = [CascadeType.ALL])
    val refusjonsgrunnlag: Refusjonsgrunnlag,
    val bedriftNr: String,
    val deltakerFnr: String
) : AbstractAggregateRoot<Refusjon>() {
    constructor(tilskuddsgrunnlag: Tilskuddsgrunnlag, bedriftNr: String, deltakerFnr: String) : this(
        Refusjonsgrunnlag(tilskuddsgrunnlag), bedriftNr, deltakerFnr
    )

    @Id
    val id: String = ULID.random()

    // Fristen er satt til 2 mnd ihht økonomireglementet
    var fristForGodkjenning: LocalDate = antallMånederEtter(refusjonsgrunnlag.tilskuddsgrunnlag.tilskuddTom, 2)
    var forrigeFristForGodkjenning: LocalDate? = null

    var godkjentAvArbeidsgiver: Instant? = null

    @Enumerated(EnumType.STRING)
    lateinit var status: RefusjonStatus

    var korreksjonId: String? = null

    // Midlertidige frontend-mappinger
    val beregning: Beregning? get() = refusjonsgrunnlag.beregning
    val tilskuddsgrunnlag: Tilskuddsgrunnlag get() = refusjonsgrunnlag.tilskuddsgrunnlag
    val inntektsgrunnlag: Inntektsgrunnlag? get() = refusjonsgrunnlag.inntektsgrunnlag
    val bedriftKontonummer: String? get() = refusjonsgrunnlag.bedriftKontonummer
    val inntekterKunFraTiltaket: Boolean? get() = refusjonsgrunnlag.inntekterKunFraTiltaket

    init {
        oppdaterStatus()
    }

    @JsonProperty
    fun harInntektIAlleMåneder(): Boolean {
        return refusjonsgrunnlag.harInntektIAlleMåneder()
    }

    private fun krevStatus(vararg gyldigeStatuser: RefusjonStatus) {
        if (status !in gyldigeStatuser) throw FeilkodeException(Feilkode.UGYLDIG_STATUS)
    }

    fun utbetalingMislykket() {
        if (status == RefusjonStatus.SENDT_KRAV || status == RefusjonStatus.UTBETALT) status =
            RefusjonStatus.UTBETALING_FEILET
    }

    fun utbetalingVellykket() {
        if (status == RefusjonStatus.SENDT_KRAV || status == RefusjonStatus.UTBETALING_FEILET) status =
            RefusjonStatus.UTBETALT
    }

    fun oppdaterStatus() {
        val statuserSomIkkeKanEndres =
            listOf(RefusjonStatus.SENDT_KRAV, RefusjonStatus.ANNULLERT, RefusjonStatus.UTBETALT)
        if (::status.isInitialized && status in statuserSomIkkeKanEndres) return

        val today = Now.localDate()
        if (today.isAfter(fristForGodkjenning)) {
            status = RefusjonStatus.UTGÅTT
            return
        }

        status = if (today.isAfter(refusjonsgrunnlag.tilskuddsgrunnlag.tilskuddTom)) {
            RefusjonStatus.KLAR_FOR_INNSENDING
        } else {
            RefusjonStatus.FOR_TIDLIG
        }
    }

    fun oppgiInntektsgrunnlag(inntektsgrunnlag: Inntektsgrunnlag) {
        oppdaterStatus()
        krevStatus(RefusjonStatus.KLAR_FOR_INNSENDING)

        this.refusjonsgrunnlag.oppgiInntektsgrunnlag(inntektsgrunnlag)
        registerEvent(InntekterInnhentet(this))
    }

    fun oppgiBedriftKontonummer(bedrifKontonummer: String) {
        refusjonsgrunnlag.oppgiBedriftKontonummer(bedrifKontonummer)
    }

    fun endreBruttolønn(inntekterKunFraTiltaket: Boolean, bruttoLønn: Int?) {
        oppdaterStatus()
        krevStatus(RefusjonStatus.KLAR_FOR_INNSENDING)
        refusjonsgrunnlag.endreBruttolønn(inntekterKunFraTiltaket, bruttoLønn)
    }

    fun godkjennForArbeidsgiver() {
        oppdaterStatus()
        krevStatus(RefusjonStatus.KLAR_FOR_INNSENDING)
        if (refusjonsgrunnlag.inntektsgrunnlag == null || refusjonsgrunnlag.inntektsgrunnlag!!.inntekter.isEmpty()) {
            throw FeilkodeException(Feilkode.INGEN_INNTEKTER)
        }
        if (refusjonsgrunnlag.bedriftKontonummer == null) {
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
        if (Now.localDate().isAfter(refusjonsgrunnlag.tilskuddsgrunnlag.tilskuddTom)) {
            status = RefusjonStatus.KLAR_FOR_INNSENDING
            registerEvent(RefusjonKlar(this))
        }
    }

    fun forkort(tilskuddTom: LocalDate, tilskuddsbeløp: Int) {
        oppdaterStatus()
        krevStatus(RefusjonStatus.KLAR_FOR_INNSENDING, RefusjonStatus.FOR_TIDLIG)
        refusjonsgrunnlag.tilskuddsgrunnlag.tilskuddTom = tilskuddTom
        refusjonsgrunnlag.tilskuddsgrunnlag.tilskuddsbeløp = tilskuddsbeløp
        oppdaterStatus()
        registerEvent(RefusjonForkortet(this))
    }

    fun opprettKorreksjonsutkast(korreksjonsgrunner: Set<Korreksjonsgrunn>): Korreksjon {
        krevStatus(RefusjonStatus.UTBETALT, RefusjonStatus.SENDT_KRAV, RefusjonStatus.UTGÅTT)
        if (korreksjonId != null) {
            throw FeilkodeException(Feilkode.HAR_KORREKSJON)
        }
        // val korreksjonsnummer = korreksjoner.size + 1
        val korreksjonsnummer = 1
        val korreksjonsutkast = Korreksjon(
            this.id,
            korreksjonsnummer,
            refusjonsgrunnlag.beregning!!.refusjonsbeløp,
            korreksjonsgrunner,
            refusjonsgrunnlag.tilskuddsgrunnlag,
            deltakerFnr,
            bedriftNr,
            refusjonsgrunnlag.inntekterKunFraTiltaket ?: true,
            refusjonsgrunnlag.endretBruttoLønn,

        )
        this.korreksjonId = korreksjonsutkast.id
        return korreksjonsutkast
    }

    fun slettKorreksjonsutkast() {
        if (korreksjonId != null) {
            korreksjonId = null
        }
    }

    fun forlengFrist(nyFrist: LocalDate, årsak: String, utførtAv: String) {
        oppdaterStatus()
        krevStatus(RefusjonStatus.FOR_TIDLIG, RefusjonStatus.KLAR_FOR_INNSENDING, RefusjonStatus.UTGÅTT)

        if (nyFrist <= fristForGodkjenning) {
            // Ny frist må være etter nåværende frist for at det skal være en forlengelse
            throw FeilkodeException(Feilkode.UGYLDIG_FORLENGELSE_AV_FRIST)
        }

        if (nyFrist > antallMånederEtter(refusjonsgrunnlag.tilskuddsgrunnlag.tilskuddTom, 6)) {
            // Kan maks forlenge 1 mnd ekstra fra opprinnelig frist på 2 mnd
            throw FeilkodeException(Feilkode.FOR_LANG_FORLENGELSE_AV_FRIST)
        }

        val gammelFristForGodkjenning = fristForGodkjenning
        forrigeFristForGodkjenning = gammelFristForGodkjenning
        fristForGodkjenning = nyFrist
        oppdaterStatus()
        registerEvent(FristForlenget(this, gammelFristForGodkjenning, fristForGodkjenning, årsak, utførtAv))
    }

    fun skalGjøreInntektsoppslag(): Boolean {
        if (status != RefusjonStatus.KLAR_FOR_INNSENDING) {
            return false
        }
        return refusjonsgrunnlag.inntektsgrunnlag?.innhentetTidspunkt?.isBefore(
            Now.localDateTime().minusMinutes(1)
        ) ?: true
    }

    fun skalGjøreKontonummerOppslag(): Boolean {
        if (status != RefusjonStatus.KLAR_FOR_INNSENDING) return false
        val innhentetTidspunkt = refusjonsgrunnlag.bedriftKontonummerInnhentetTidspunkt
        return innhentetTidspunkt == null || innhentetTidspunkt.isBefore(Now.localDateTime().minusMinutes(1))
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Refusjon

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    fun copy(
        tilskuddsgrunnlag: Tilskuddsgrunnlag = this.tilskuddsgrunnlag,
        deltakerFnr: String = this.deltakerFnr
    ): Refusjon {
        return Refusjon(tilskuddsgrunnlag, bedriftNr, deltakerFnr)
    }
}