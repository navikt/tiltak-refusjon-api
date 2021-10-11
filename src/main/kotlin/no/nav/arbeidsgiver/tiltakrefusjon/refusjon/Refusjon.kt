package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import com.fasterxml.jackson.annotation.JsonProperty
import com.github.guepardoapps.kulid.ULID
import no.nav.arbeidsgiver.tiltakrefusjon.Feilkode
import no.nav.arbeidsgiver.tiltakrefusjon.FeilkodeException
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.events.*
import no.nav.arbeidsgiver.tiltakrefusjon.utils.Now
import no.nav.arbeidsgiver.tiltakrefusjon.utils.antallMånederEtter
import org.springframework.data.domain.AbstractAggregateRoot
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.util.*
import javax.persistence.*
import kotlin.streams.toList

@Entity
data class Refusjon(
    @OneToOne(orphanRemoval = true, cascade = [CascadeType.ALL])
    val tilskuddsgrunnlag: Tilskuddsgrunnlag,
    val bedriftNr: String,
    val deltakerFnr: String,
    val korreksjonAvId: String? = null,
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
    fun korrigertBruttoLønn() = endretBruttoLønn

    @JsonProperty
    fun harInntektIAlleMåneder(): Boolean {
        val månederInntekter =
            inntektsgrunnlag?.inntekter?.filter { it.erMedIInntektsgrunnlag() }?.map { it.måned }?.sorted()?.distinct()
                ?: emptyList()

        val tilskuddFom = tilskuddsgrunnlag.tilskuddFom
        val tilskuddTom = tilskuddsgrunnlag.tilskuddTom

        val månederTilskudd =
            tilskuddFom.datesUntil(tilskuddTom).map { YearMonth.of(it.year, it.month) }.distinct().toList()

        return månederInntekter.containsAll(månederTilskudd)
    }

    private fun krevStatus(vararg gyldigeStatuser: RefusjonStatus) {
        if (status !in gyldigeStatuser) throw FeilkodeException(Feilkode.UGYLDIG_STATUS)
    }

    fun oppdaterStatus() {
        val statuserSomIkkeKanEndres =
            listOf(RefusjonStatus.SENDT_KRAV, RefusjonStatus.ANNULLERT, RefusjonStatus.UTBETALT)
        if (::status.isInitialized && status in statuserSomIkkeKanEndres) return

        if (korreksjonAvId != null) {
            status = RefusjonStatus.MANUELL_KORREKSJON
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
        krevStatus(RefusjonStatus.KLAR_FOR_INNSENDING, RefusjonStatus.MANUELL_KORREKSJON)
        this.inntektsgrunnlag = inntektsgrunnlag
        registerEvent(InntekterInnhentet(this))
    }

        fun endreBruttolønn(inntekterKunFraTiltaket: Boolean, bruttoLønn: Int?) {
        oppdaterStatus()
        krevStatus(RefusjonStatus.KLAR_FOR_INNSENDING, RefusjonStatus.MANUELL_KORREKSJON)
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
            beregning = beregnRefusjonsbeløp(inntektsgrunnlag!!.inntekter.toList(), tilskuddsgrunnlag, appImageId,
                tidligereUtbetalt,
                endretBruttoLønn)
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

    fun lagKorreksjon(korreksjonsgrunner: Set<Korreksjonsgrunn>): Refusjon {
        krevStatus(RefusjonStatus.UTBETALT, RefusjonStatus.SENDT_KRAV, RefusjonStatus.UTGÅTT)
        if (korrigeresAvId != null) {
            throw FeilkodeException(Feilkode.HAR_KORREKSJON)
        }
        val korreksjon = Refusjon(Tilskuddsgrunnlag(this.tilskuddsgrunnlag), this.bedriftNr, this.deltakerFnr, this.id)
        val kopiAvInntektsgrunnlag = Inntektsgrunnlag(
            inntekter = this.inntektsgrunnlag!!.inntekter.map {
                Inntektslinje(it.inntektType,
                    it.beskrivelse,
                    it.beløp,
                    it.måned,
                    it.opptjeningsperiodeFom,
                    it.opptjeningsperiodeTom)
            }, respons = this.inntektsgrunnlag!!.respons)
        kopiAvInntektsgrunnlag.innhentetTidspunkt = this.inntektsgrunnlag!!.innhentetTidspunkt
        korreksjon.inntektsgrunnlag = kopiAvInntektsgrunnlag
        korreksjon.bedriftKontonummer = this.bedriftKontonummer
        korreksjon.korreksjonsgrunner.addAll(korreksjonsgrunner)
        this.korrigeresAvId = korreksjon.id
        return korreksjon
    }

    fun kanSlettes(): Boolean {
        return status == RefusjonStatus.MANUELL_KORREKSJON
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