package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import com.github.guepardoapps.kulid.ULID
import no.nav.arbeidsgiver.tiltakrefusjon.Feilkode
import no.nav.arbeidsgiver.tiltakrefusjon.FeilkodeException
import no.nav.arbeidsgiver.tiltakrefusjon.utils.Now
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
    val tidligereUtbetalt: Int = 0,
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
    var minusBeløpFraForrigeRefusjon: Int? = null

    @OneToOne(orphanRemoval = true, cascade = [CascadeType.ALL])
    var beregning: Beregning? = null

    fun oppgiInntektsgrunnlag(
        inntektsgrunnlag: Inntektsgrunnlag,
        gjeldendeInntektsgrunnlag: Inntektsgrunnlag?
    ): Boolean {
        if (gjeldendeInntektsgrunnlag != null) {
            inntektsgrunnlag.inntekter.forEach { inntekt ->
                val gjeldendeInntektslinje = gjeldendeInntektsgrunnlag.inntekter
                    .find { it.beløp == inntekt.beløp && it.måned == inntekt.måned && it.beskrivelse == inntekt.beskrivelse }
                if (gjeldendeInntektslinje != null) {
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
        return minusBeløpFraForrigeRefusjon != null && bedriftKontonummer != null && (inntekterKunFraTiltaket == true && endretBruttoLønn == null ||
                ((inntekterKunFraTiltaket == false || inntekterKunFraTiltaket == null) && endretBruttoLønn != null))
    }

    fun refusjonsgrunnlagetErPositivt(): Boolean {
        return this.beregning?.refusjonsbeløp != null && this.beregning!!.refusjonsbeløp > 0
    }

    private fun gjørBeregning(): Boolean {
        if (erAltOppgitt()) {
            this.beregning = beregnRefusjonsbeløp(
                inntekter = inntektsgrunnlag!!.inntekter.toList(),
                tilskuddsgrunnlag = tilskuddsgrunnlag,
                tidligereUtbetalt = tidligereUtbetalt,
                korrigertBruttoLønn = endretBruttoLønn,
                fratrekkRefunderbarSum = refunderbarBeløp,
                minus
            )
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
