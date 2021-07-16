package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import com.fasterxml.jackson.annotation.JsonProperty
import com.github.guepardoapps.kulid.ULID
import no.nav.arbeidsgiver.tiltakrefusjon.Feilkode
import no.nav.arbeidsgiver.tiltakrefusjon.FeilkodeException
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.events.*
import no.nav.arbeidsgiver.tiltakrefusjon.utils.Now
import org.springframework.data.domain.AbstractAggregateRoot
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
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

    // Fristen er satt til 2 mnd ihht økonomireglementet
    var fristForGodkjenning: LocalDate = tilskuddsgrunnlag.tilskuddTom.plusMonths(2)

    var godkjentAvArbeidsgiver: Instant? = null
    var godkjentAvSaksbehandler: Instant? = null

    var bedriftKontonummer: String? = null
    var innhentetBedriftKontonummerTidspunkt: LocalDateTime? = null

    @Enumerated(EnumType.STRING)
    lateinit var status: RefusjonStatus

    init {
        oppdaterStatus()
    }

    @JsonProperty
    fun harInntektIAlleMåneder(): Boolean {
        val måneder = inntektsgrunnlag?.inntekter?.filter { it.erMedIInntektsgrunnlag() }?.map { it.måned }?.sorted()
        val tilskuddFom = tilskuddsgrunnlag.tilskuddFom
        val tilskuddTom = tilskuddsgrunnlag.tilskuddTom
        return !måneder.isNullOrEmpty() && måneder.first() == YearMonth.from(tilskuddFom) && måneder.last() == YearMonth.from(
            tilskuddTom)
    }

    private fun krevStatus(vararg gyldigeStatuser: RefusjonStatus) {
        if (status !in gyldigeStatuser) throw FeilkodeException(Feilkode.UGYLDIG_STATUS)
    }


    fun oppdaterStatus() {
        val statuserSomIkkeKanEndres =
            listOf(RefusjonStatus.SENDT_KRAV, RefusjonStatus.ANNULLERT, RefusjonStatus.UTGÅTT, RefusjonStatus.UTBETALT)
        if (::status.isInitialized && status in statuserSomIkkeKanEndres) return

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

    fun oppgiInntektsgrunnlag(inntektsgrunnlag: Inntektsgrunnlag, appImageId: String) {
        oppdaterStatus()
        krevStatus(RefusjonStatus.KLAR_FOR_INNSENDING)

        if (inntektsgrunnlag.inntekter.isNotEmpty()) {
            beregning = beregnRefusjonsbeløp(inntektsgrunnlag.inntekter, tilskuddsgrunnlag, appImageId)
        }
        this.inntektsgrunnlag = inntektsgrunnlag
        registerEvent(InntekterInnhentet(this))
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

}