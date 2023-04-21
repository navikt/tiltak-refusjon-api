package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import com.github.guepardoapps.kulid.ULID
import no.nav.arbeidsgiver.tiltakrefusjon.Feilkode
import no.nav.arbeidsgiver.tiltakrefusjon.FeilkodeException
import no.nav.arbeidsgiver.tiltakrefusjon.utils.Now
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import java.time.YearMonth
import javax.persistence.CascadeType
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.OneToOne

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
    var bedriftKontonummerInnhentetTidspunkt: LocalDateTime? = null
    var inntekterKunFraTiltaket: Boolean? = null
    var endretBruttoLønn: Int? = null
    var fratrekkRefunderbarBeløp: Boolean? = null
    var refunderbarBeløp: Int? = null
    var forrigeRefusjonMinusBeløp: Int = 0

    @OneToOne(orphanRemoval = true, cascade = [CascadeType.ALL])
    var beregning: Beregning? = null

    fun oppgiInntektsgrunnlag(
        inntektsgrunnlag: Inntektsgrunnlag,
        gjeldendeInntektsgrunnlag: Inntektsgrunnlag?
    ): Boolean {
        val log = LoggerFactory.getLogger(javaClass)
        if (gjeldendeInntektsgrunnlag != null) {
            inntektsgrunnlag.inntekter.forEach { inntekt ->
                val gjeldendeInntektslinje = finnInntektslinjeIListeMedInntekter(inntekt, gjeldendeInntektsgrunnlag.inntekter)
                if (gjeldendeInntektslinje != null) {
                    // inntekt er identisk med en inntekt fra tidligere inntektsgrunnlag (gjeldendeInntektslinje)
                    inntekt.erOpptjentIPeriode = gjeldendeInntektslinje.erOpptjentIPeriode
                }
            }
        }
        if(inntektsgrunnlag.inntekter.filter { it.erMedIInntektsgrunnlag() }.find { it.erOpptjentIPeriode === null } !== null) {
            this.resetEndreBruttolønn()
        }
        this.inntektsgrunnlag = inntektsgrunnlag
        return gjørBeregning()
    }

    fun finnInntektslinjeIListeMedInntekter(linje1: Inntektslinje, inntektslinjer: Set<Inntektslinje>): Inntektslinje? {
        return inntektslinjer.find {
                    it.inntektType == linje1.inntektType &&
                    it.beskrivelse == linje1.beskrivelse &&
                    it.beløp == linje1.beløp &&
                    it.måned == linje1.måned &&
                    it.opptjeningsperiodeFom == linje1.opptjeningsperiodeFom &&
                    it.opptjeningsperiodeTom == linje1.opptjeningsperiodeTom
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

    fun harInntektIAlleMåneder(): Boolean {
        val månederInntekter =
            inntektsgrunnlag?.inntekter?.filter { it.erMedIInntektsgrunnlag() }?.map { it.måned }
                ?.sorted()?.distinct()
                ?: emptyList()

        val tilskuddFom = tilskuddsgrunnlag.tilskuddFom
        val tilskuddTom = tilskuddsgrunnlag.tilskuddTom

        val månederTilskudd =
        tilskuddFom.datesUntil(tilskuddTom).map { YearMonth.of(it.year, it.month) }.distinct().toList()

        return månederInntekter.containsAll(månederTilskudd)
    }

    fun setInntektslinjeTilOpptjentIPeriode(inntekslinjeId: String, erOpptjentIPeriode: Boolean): Boolean {
        val inntektslinje = inntektsgrunnlag?.inntekter?.find { it.id == inntekslinjeId }
            ?: throw RuntimeException("Finner ikke inntektslinje med id=$id")
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
