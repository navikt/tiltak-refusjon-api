package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import com.github.guepardoapps.kulid.ULID
import no.nav.arbeidsgiver.tiltakrefusjon.Feilkode
import no.nav.arbeidsgiver.tiltakrefusjon.FeilkodeException
import no.nav.arbeidsgiver.tiltakrefusjon.utils.Now
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import javax.persistence.*

@Entity
class Refusjonsgrunnlag(
    @ManyToOne(cascade = [CascadeType.PERSIST, CascadeType.MERGE], optional = false)
    @JoinColumn(name = "tilskuddsgrunnlag_id")
    val tilskuddsgrunnlag: Tilskuddsgrunnlag,
    val tidligereUtbetalt: Int = 0
) {
    @Id
    val id = ULID.random()

    @OneToOne(orphanRemoval = true, cascade = [CascadeType.ALL])
    var inntektsgrunnlag: Inntektsgrunnlag? = null
    var bedriftKontonummer: String? = null
    var bedriftKid: String? = null
    var bedriftKontonummerInnhentetTidspunkt: LocalDateTime? = null
    var inntekterKunFraTiltaket: Boolean? = null
    var endretBruttoLønn: Int? = null
    var fratrekkRefunderbarBeløp: Boolean? = null
    var refunderbarBeløp: Int? = null
    var forrigeRefusjonMinusBeløp: Int = 0

    @OneToOne(orphanRemoval = true, cascade = [CascadeType.ALL])
    var beregning: Beregning? = null

    fun oppgiInntektsgrunnlag(
        nyInntektsgrunnlag: Inntektsgrunnlag,
        gjeldendeInntektsgrunnlag: Inntektsgrunnlag?
    ): Boolean {
        val log = LoggerFactory.getLogger(javaClass)
        val nyInntektsgrunnlag = hentOppdatertInnteksgrunnlag(nyInntektsgrunnlag,gjeldendeInntektsgrunnlag)
        if(nyInntektsgrunnlag.inntekter.filter { it.erMedIInntektsgrunnlag() }.find { it.erOpptjentIPeriode === null } !== null) {
            this.resetEndreBruttolønn()
        }
        this.inntektsgrunnlag = nyInntektsgrunnlag
        return gjørBeregning()
    }

    fun hentOppdatertInnteksgrunnlag(nyInntektsgrunnlag: Inntektsgrunnlag,
                                     gjeldendeInntektsgrunnlag: Inntektsgrunnlag?):Inntektsgrunnlag{

        // IKKE OVERSKRIV (ID) ELDRE INNTEKTER OM NYE INNTEKTER FRA AMELDING ER LIK ELDRE;
        val eldreInntekter = gjeldendeInntektsgrunnlag?.inntekter
        val nyeInntekter = nyInntektsgrunnlag.inntekter
        if(!eldreInntekter.isNullOrEmpty() && !nyeInntekter.isNullOrEmpty()){
            // merge KUN ULIKE eldre og nye inntektslinjer
            val nyeInntekter = nyeInntekter.filter { nyInntekt ->
                eldreInntekter.none { eldreInntekt -> eldreInntekt.beløp.equals(nyInntekt.beløp)
                        && eldreInntekt.beskrivelse.equals(nyInntekt.beskrivelse)
                        && eldreInntekt.inntektType == nyInntekt.inntektType
                        && eldreInntekt.måned == nyInntekt.måned
                        && eldreInntekt.opptjeningsperiodeFom?.isEqual(nyInntekt.opptjeningsperiodeFom) ?: true
                        && eldreInntekt.opptjeningsperiodeTom?.isEqual(nyInntekt.opptjeningsperiodeTom) ?: true
                }}
            return Inntektsgrunnlag(
                inntekter = eldreInntekter.plus(nyeInntekter),
                respons = nyInntektsgrunnlag.respons.plus(gjeldendeInntektsgrunnlag.respons)
            )
        }

        return Inntektsgrunnlag(
                inntekter = nyeInntekter,
                respons = nyInntektsgrunnlag.respons
            )

    }

    fun finnInntektslinjeIListeMedInntekter(nyInntektLinje: Inntektslinje, gjeldendeInntektslinjer: Set<Inntektslinje>): Inntektslinje? {
        return gjeldendeInntektslinjer.find {
                    it.inntektType == nyInntektLinje.inntektType &&
                    it.beskrivelse == nyInntektLinje.beskrivelse &&
                    it.beløp == nyInntektLinje.beløp &&
                    it.måned == nyInntektLinje.måned &&
                    it.opptjeningsperiodeFom == nyInntektLinje.opptjeningsperiodeFom &&
                    it.opptjeningsperiodeTom == nyInntektLinje.opptjeningsperiodeTom
        }
    }

    fun oppgiForrigeRefusjonsbeløp(forrigeRefusjonMinusBeløp: Int): Boolean{
        this.forrigeRefusjonMinusBeløp = forrigeRefusjonMinusBeløp
        return gjørBeregning()
    }

    fun oppgiBedriftKontonummer(bedrifKontonummer: String?): Boolean {
        this.bedriftKontonummer = bedrifKontonummer
        this.bedriftKontonummerInnhentetTidspunkt = Now.localDateTime()
        return gjørBeregning()
    }

    fun resetEndreBruttolønn() {
        this.inntekterKunFraTiltaket = null
        this.endretBruttoLønn = null
        this.fratrekkRefunderbarBeløp = null
        this.refunderbarBeløp = null
        this.beregning = null
    }

    fun endreBruttolønn(inntekterKunFraTiltaket: Boolean?, bruttoLønn: Int?): Boolean {
        if (inntekterKunFraTiltaket != null && inntekterKunFraTiltaket == true && bruttoLønn != null) {
            throw FeilkodeException(Feilkode.INNTEKTER_KUN_FRA_TILTAK_OG_OPPGIR_BELØP)
        }
        this.inntekterKunFraTiltaket = inntekterKunFraTiltaket
        this.endretBruttoLønn = bruttoLønn
        return gjørBeregning()
    }

    fun erAltOppgitt(): Boolean {
        val inntektsgrunnlag = inntektsgrunnlag
        if (inntektsgrunnlag == null || inntektsgrunnlag.inntekter.none { it.erMedIInntektsgrunnlag() }) return false
        return bedriftKontonummer != null && (inntekterKunFraTiltaket == true && endretBruttoLønn == null ||
                ((inntekterKunFraTiltaket == false || inntekterKunFraTiltaket == null) && endretBruttoLønn != null))
    }

    fun refusjonsgrunnlagetErPositivt(): Boolean {
        return this.beregning?.refusjonsbeløp != null && this.beregning!!.refusjonsbeløp > 0
    }

    fun refusjonsgrunnlagetErNullSomIZero(): Boolean {
        return this.beregning?.refusjonsbeløp != null && this.beregning!!.refusjonsbeløp == 0
    }

    private fun gjørBeregning(): Boolean {
        if (erAltOppgitt()) {
            this.beregning = beregnRefusjonsbeløp(
                inntekter = inntektsgrunnlag!!.inntekter.toList(),
                tilskuddsgrunnlag = tilskuddsgrunnlag,
                tidligereUtbetalt = tidligereUtbetalt,
                korrigertBruttoLønn = endretBruttoLønn,
                fratrekkRefunderbarSum = refunderbarBeløp,
            forrigeRefusjonMinusBeløp = forrigeRefusjonMinusBeløp)
            return true
        }
        return false
    }

    fun setInntektslinjeTilOpptjentIPeriode(inntekslinjeId: String, erOpptjentIPeriode: Boolean): Boolean {
        /* FRONTEND HAR INTEKSLINJE MEN BACKEND SIER DEN IKKE ER DER. */
        val inntektslinje = inntektsgrunnlag?.inntekter?.find { it.id == inntekslinjeId }
            ?: throw RuntimeException("Finner ikke inntektslinje med id=$inntekslinjeId for refusjongrunnlag=$id")
        if (!inntektslinje.erMedIInntektsgrunnlag()) {
            throw FeilkodeException(Feilkode.INNTEKTSLINJE_IKKE_MED_I_GRUNNLAG)
        }
        inntektslinje.erOpptjentIPeriode = erOpptjentIPeriode

        var erNoenOpptjentIPerioden = false
        inntektsgrunnlag?.inntekter?.forEach {
            if(it.erOpptjentIPeriode == true) {
                erNoenOpptjentIPerioden = true
            }
        }

        if(!erNoenOpptjentIPerioden) {
            beregning = null
            endretBruttoLønn = null
            fratrekkRefunderbarBeløp = null
            inntekterKunFraTiltaket = null
            return false
        }

        return gjørBeregning()
    }

    fun settFratrekkRefunderbarBeløp(fratrekkRefunderbarBeløp: Boolean, refunderbarBeløp: Int?): Boolean {
        if (!fratrekkRefunderbarBeløp && refunderbarBeløp != null) {
            throw FeilkodeException(Feilkode.INNTEKTER_KUN_FRA_TILTAK_OG_OPPGIR_BELØP)
        }
        this.fratrekkRefunderbarBeløp = fratrekkRefunderbarBeløp
        this.refunderbarBeløp = refunderbarBeløp
        return gjørBeregning()
    }
}
