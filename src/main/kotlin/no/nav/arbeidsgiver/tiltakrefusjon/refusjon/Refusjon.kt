package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.persistence.*
import no.nav.arbeidsgiver.tiltakrefusjon.Feilkode
import no.nav.arbeidsgiver.tiltakrefusjon.FeilkodeException
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.events.*
import no.nav.arbeidsgiver.tiltakrefusjon.tilskuddsperiode.MidlerFrigjortÅrsak
import no.nav.arbeidsgiver.tiltakrefusjon.utils.KidValidator
import no.nav.arbeidsgiver.tiltakrefusjon.utils.Now
import no.nav.arbeidsgiver.tiltakrefusjon.utils.antallMånederEtter
import no.nav.arbeidsgiver.tiltakrefusjon.utils.ulid
import org.springframework.data.domain.AbstractAggregateRoot
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
class Refusjon(
    @OneToOne(orphanRemoval = true, cascade = [CascadeType.ALL])
    val refusjonsgrunnlag: Refusjonsgrunnlag,
    val bedriftNr: String,
    val deltakerFnr: String
) : AbstractAggregateRoot<Refusjon>() {
    constructor(
        tilskuddsgrunnlag: Tilskuddsgrunnlag,
        bedriftNr: String,
        deltakerFnr: String,
    ) : this(
        Refusjonsgrunnlag(tilskuddsgrunnlag), bedriftNr, deltakerFnr
    )

    @Id
    val id: String = ulid()

    // Fristen er satt til 2 mnd ihht reimplementation. Hvis etterregistrert 2 mnd etter godkjent tidspunkt av beslutter
    var fristForGodkjenning: LocalDate = lagFristForGodkjenning()

    var forrigeFristForGodkjenning: LocalDate? = null

    var unntakOmInntekterFremitid: Int = 0
    var hentInntekterLengerFrem: LocalDateTime? = null

    var godkjentAvArbeidsgiver: Instant? = null

    @Enumerated(EnumType.STRING)
    lateinit var status: RefusjonStatus

    var korreksjonId: String? = null

    @Enumerated(EnumType.STRING)
    var midlerFrigjortÅrsak: MidlerFrigjortÅrsak? = null

    // Midlertidige frontend-mappinger
    val bedriftKontonummer: String? get() = refusjonsgrunnlag.bedriftKontonummer
    val inntekterKunFraTiltaket: Boolean? get() = refusjonsgrunnlag.inntekterKunFraTiltaket
    var utbetaltTidspunkt: Instant? = null
    var åpnetFørsteGang: Instant? = null
    @OneToOne(orphanRemoval = true, cascade = [CascadeType.ALL])
    var minusbelop: Minusbelop? = null
    init {
        oppdaterStatus()
    }

    fun lagFristForGodkjenning() : LocalDate {
        if (refusjonsgrunnlag.tilskuddsgrunnlag.godkjentAvBeslutterTidspunkt == null) {
            return antallMånederEtter(refusjonsgrunnlag.tilskuddsgrunnlag.tilskuddTom, 2)
        }
        if (refusjonsgrunnlag.tilskuddsgrunnlag.godkjentAvBeslutterTidspunkt.toLocalDate().isAfter(refusjonsgrunnlag.tilskuddsgrunnlag.tilskuddTom)) {
            return antallMånederEtter(refusjonsgrunnlag.tilskuddsgrunnlag.godkjentAvBeslutterTidspunkt.toLocalDate(), 2)
        } else {
            return antallMånederEtter(refusjonsgrunnlag.tilskuddsgrunnlag.tilskuddTom, 2)
        }
    }

    @JsonProperty
    fun harTattStillingTilAlleInntektslinjer(): Boolean =
        refusjonsgrunnlag.inntektsgrunnlag?.inntekter?.filter { it.erMedIInntektsgrunnlag() }?.find { inntekt -> inntekt.erOpptjentIPeriode === null } === null

    private fun krevStatus(vararg gyldigeStatuser: RefusjonStatus) {
        if (status !in gyldigeStatuser) throw FeilkodeException(Feilkode.UGYLDIG_STATUS)
    }

    fun utbetalingMislykket() {
        if (status == RefusjonStatus.SENDT_KRAV || status == RefusjonStatus.UTBETALT) {
            status = RefusjonStatus.UTBETALING_FEILET
            registerEvent(RefusjonEndretStatus(this))
        }
    }

    fun utbetalingVellykket() {
        if (status == RefusjonStatus.SENDT_KRAV || status == RefusjonStatus.UTBETALING_FEILET) {
            status = RefusjonStatus.UTBETALT
            utbetaltTidspunkt = Now.instant()
            registerEvent(RefusjonEndretStatus(this))
        }
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

    fun oppgiInntektsgrunnlag(inntektsgrunnlag: Inntektsgrunnlag, gjeldendeInntektsgrunnlag: Inntektsgrunnlag? = null) {
        oppdaterStatus()
        krevStatus(RefusjonStatus.KLAR_FOR_INNSENDING)

        val harGjortBeregning = this.refusjonsgrunnlag.oppgiInntektsgrunnlag(inntektsgrunnlag, gjeldendeInntektsgrunnlag)
        if (harGjortBeregning) {
            registerEvent(BeregningUtført(this))
        }
    }

    fun oppgiBedriftKontonummer(bedrifKontonummer: String?) {
        refusjonsgrunnlag.oppgiBedriftKontonummer(bedrifKontonummer)
    }

    fun endreBruttolønn(inntekterKunFraTiltaket: Boolean?, bruttoLønn: Int?) {
        oppdaterStatus()
        krevStatus(RefusjonStatus.KLAR_FOR_INNSENDING)
        val harGjortBeregning = refusjonsgrunnlag.endreBruttolønn(inntekterKunFraTiltaket, bruttoLønn)
        if (harGjortBeregning) {
            registerEvent(BeregningUtført(this))
        }
    }

    fun endreBedriftKID( bedriftKID: String?) {
        oppdaterStatus()
        krevStatus(RefusjonStatus.KLAR_FOR_INNSENDING)
        refusjonsgrunnlag.bedriftKid = bedriftKID
    }

    fun godkjennForArbeidsgiver(utførtAv: String) {
        oppdaterStatus()
        krevStatus(RefusjonStatus.KLAR_FOR_INNSENDING)

        if(!refusjonsgrunnlag.bedriftKid?.trim().isNullOrEmpty()){
            KidValidator(refusjonsgrunnlag.bedriftKid)
        }
        if (refusjonsgrunnlag.inntektsgrunnlag == null || refusjonsgrunnlag.inntektsgrunnlag!!.inntekter.isEmpty()) {
            throw FeilkodeException(Feilkode.INGEN_INNTEKTER)
        }
        if (refusjonsgrunnlag.bedriftKontonummer == null) {
            throw FeilkodeException(Feilkode.INGEN_BEDRIFTKONTONUMMER)
        }
        if (!this.harTattStillingTilAlleInntektslinjer()) {
            throw FeilkodeException(Feilkode.IKKE_TATT_STILLING_TIL_ALLE_INNTEKTSLINJER)
        }
        godkjentAvArbeidsgiver = Now.instant()
        status = RefusjonStatus.SENDT_KRAV

        if(refusjonsgrunnlag.refusjonsgrunnlagetErNullSomIZero()) {
            status = RefusjonStatus.GODKJENT_NULLBELØP
            registerEvent(RefusjonGodkjentNullBeløp(this, utførtAv))
        } else if(!refusjonsgrunnlag.refusjonsgrunnlagetErPositivt()) {
            status = RefusjonStatus.GODKJENT_MINUSBELØP
            registerEvent(RefusjonGodkjentMinusBeløp(this, utførtAv))
        } else {
            registerEvent(GodkjentAvArbeidsgiver(this, utførtAv))
        }

        registerEvent(RefusjonEndretStatus(this))
    }

    fun godkjennNullbeløpForArbeidsgiver(utførtAv: String) {
        oppdaterStatus()
        krevStatus(RefusjonStatus.KLAR_FOR_INNSENDING)

        if(!refusjonsgrunnlag.bedriftKid?.trim().isNullOrEmpty()){
            KidValidator(refusjonsgrunnlag.bedriftKid)
        }
        godkjentAvArbeidsgiver = Now.instant()
        status = RefusjonStatus.GODKJENT_NULLBELØP
        registerEvent(RefusjonGodkjentNullBeløp(this, utførtAv))
        registerEvent(RefusjonEndretStatus(this))
    }

    /**
     * Annullerer en refusjon
     *
     * Normalt vil ikke en refusjon kunne annulleres med refusjonen er "tidlig i løpet"
     * (feks vil det være for sent å annullere dersom statusen er "UTBETALT").
     *
     * Skal det allikevel være nødvendig å annullere en refusjon uansett status, kan man
     * sette "tving"-flagg til true
     */
    fun annuller(tving: Boolean = false) {
        oppdaterStatus()
        if (!tving) {
            krevStatus(RefusjonStatus.KLAR_FOR_INNSENDING, RefusjonStatus.FOR_TIDLIG)
        }
        if (status == RefusjonStatus.ANNULLERT) {
            return
        }
        status = RefusjonStatus.ANNULLERT
        registerEvent(RefusjonAnnullert(this))
        registerEvent(RefusjonEndretStatus(this))
    }

    fun annullerTilskuddsperioderIRefusjon(utførtAv: String, grunn: String) {
        // Midler som er holdt av skal frigjøres når refusjonsfristen er utgått. enten manuelt eller automatisk.
        // Foreløpig kun manuelt.
        oppdaterStatus()
        krevStatus(RefusjonStatus.UTGÅTT)
        registerEvent(TilskuddsperioderIRefusjonAnnullertManuelt(this, utførtAv, grunn))
    }

    fun gjørKlarTilInnsending() {
        krevStatus(RefusjonStatus.FOR_TIDLIG)
        if (Now.localDate().isAfter(refusjonsgrunnlag.tilskuddsgrunnlag.tilskuddTom)) {
            status = RefusjonStatus.KLAR_FOR_INNSENDING
            registerEvent(RefusjonEndretStatus(this))
        }
    }
    fun gjørRefusjonUtgått() {
        krevStatus(RefusjonStatus.KLAR_FOR_INNSENDING)
        if (Now.localDate().isAfter(fristForGodkjenning)) {
            status = RefusjonStatus.UTGÅTT
            registerEvent(RefusjonUtgått(this))
            registerEvent(RefusjonEndretStatus(this))
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

fun opprettKorreksjonsutkast(korreksjonsgrunner: Set<Korreksjonsgrunn>, unntakOmInntekterFremitid: Int?, annenGrunn: String?): Korreksjon {
        krevStatus(RefusjonStatus.UTBETALT, RefusjonStatus.SENDT_KRAV,RefusjonStatus.GODKJENT_MINUSBELØP, RefusjonStatus.UTGÅTT)
        if (korreksjonId != null) {
            throw FeilkodeException(Feilkode.HAR_KORREKSJON)
        }
        val korreksjonsnummer = 1
        val tidligereUtbetalt = if (korreksjonsgrunner.contains(Korreksjonsgrunn.UTBETALT_HELE_TILSKUDDSBELØP)) refusjonsgrunnlag.tilskuddsgrunnlag.tilskuddsbeløp else refusjonsgrunnlag.beregning!!.refusjonsbeløp
        val korreksjonsutkast = Korreksjon(
            korrigererRefusjonId = this.id,
            korreksjonsnummer = korreksjonsnummer,
            tidligereUtbetalt = tidligereUtbetalt,
            korreksjonsgrunner = korreksjonsgrunner,
            tilskuddsgrunnlag = refusjonsgrunnlag.tilskuddsgrunnlag,
            deltakerFnr = deltakerFnr,
            bedriftNr = bedriftNr,
            inntekterKunFraTiltaket = refusjonsgrunnlag.inntekterKunFraTiltaket ?: true,
            endretBruttoLønn = refusjonsgrunnlag.endretBruttoLønn,
            unntakOmInntekterFremitid = unntakOmInntekterFremitid,
            annenGrunn = annenGrunn
        )
        this.korreksjonId = korreksjonsutkast.id
        return korreksjonsutkast
    }

    fun fjernKorreksjonId() {
        // TODO: Kreve at status ikke er korrigert
        if (korreksjonId != null) {
            korreksjonId = null
        }
    }

    fun forlengFrist(nyFrist: LocalDate, årsak: String, utførtAv: String) {
        forlengFrist(nyFrist, årsak, utførtAv, false)
    }

    fun forlengFrist(nyFrist: LocalDate, årsak: String, utførtAv: String, enforce: Boolean) {
        oppdaterStatus()
        krevStatus(RefusjonStatus.FOR_TIDLIG, RefusjonStatus.KLAR_FOR_INNSENDING)

        if (nyFrist <= fristForGodkjenning) {
            // Ny frist må være etter nåværende frist for at det skal være en forlengelse
            throw FeilkodeException(Feilkode.UGYLDIG_FORLENGELSE_AV_FRIST)
        }

        // Opprinnelig frist er er 2 mnd. Det er enten 2 mnd etter tilskuddTom eller 2 mnd etter godkjentAvBeslutterTidspunkt.
        // Maks forlengelse er 1 mnd.
        val opprinneligFrist = lagFristForGodkjenning()
        val kanForlengeSykepenger = refusjonsgrunnlag.fratrekkRefunderbarBeløp == true && nyFrist < antallMånederEtter(opprinneligFrist, 12)
        println("Har ikke peiling $kanForlengeSykepenger")
        if (!(enforce || kanForlengeSykepenger) && (nyFrist > antallMånederEtter(opprinneligFrist, 1))) {
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
        if (hentInntekterLengerFrem != null && refusjonsgrunnlag.inntektsgrunnlag?.innhentetTidspunkt?.isBefore(hentInntekterLengerFrem) ?: true) {
            return true
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

    fun merkForUnntakOmInntekterToMånederFrem(merking: Int) {
        krevStatus(RefusjonStatus.FOR_TIDLIG, RefusjonStatus.KLAR_FOR_INNSENDING)
        if(merking == 1 && hentInntekterLengerFrem != null) {
            throw FeilkodeException(Feilkode.HAR_ALLERDE_UNNTAK_OM_INNTEKTER_1_MND_FREM)
        }
        unntakOmInntekterFremitid = merking
    }

    fun merkForHentInntekterFrem(merking: Boolean, utførtAv: String) {
        krevStatus(RefusjonStatus.KLAR_FOR_INNSENDING)
        if (unntakOmInntekterFremitid > 0) {
            throw FeilkodeException(Feilkode.HAR_ALLERDE_UNNTAK_OM_INNTEKTER_2_MND_FREM)
        }

        if (merking) {
            hentInntekterLengerFrem = Now.localDateTime()
        } else {
            hentInntekterLengerFrem = null
        }
        registerEvent(MerketForInntekterFrem(this, merking, utførtAv))
    }

    fun setInntektslinjeTilOpptjentIPeriode(inntekslinjeId: String, erOpptjentIPeriode: Boolean) {
        oppdaterStatus()
        krevStatus(RefusjonStatus.KLAR_FOR_INNSENDING)
        var harGjortBeregning  = refusjonsgrunnlag.setInntektslinjeTilOpptjentIPeriode(inntekslinjeId, erOpptjentIPeriode)
        if (harGjortBeregning) {
            registerEvent(BeregningUtført(this))
        }
    }

    fun settFratrekkRefunderbarBeløp(fratrekkRefunderbarBeløp: Boolean, refunderbarBeløp: Int?) {
        oppdaterStatus()
        krevStatus(RefusjonStatus.KLAR_FOR_INNSENDING)
        val harGjortBeregning = refusjonsgrunnlag.settFratrekkRefunderbarBeløp(fratrekkRefunderbarBeløp, refunderbarBeløp)
        if (harGjortBeregning) {
            registerEvent(BeregningUtført(this))
        }
    }

}
