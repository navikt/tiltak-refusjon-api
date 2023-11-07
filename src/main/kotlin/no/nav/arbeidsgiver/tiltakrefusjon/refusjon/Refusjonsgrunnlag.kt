package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import jakarta.persistence.*
import no.nav.arbeidsgiver.tiltakrefusjon.Feilkode
import no.nav.arbeidsgiver.tiltakrefusjon.FeilkodeException
import no.nav.arbeidsgiver.tiltakrefusjon.utils.Now
import no.nav.arbeidsgiver.tiltakrefusjon.utils.ulid
import java.time.LocalDateTime

@Entity
class Refusjonsgrunnlag(
    @ManyToOne(cascade = [CascadeType.PERSIST, CascadeType.MERGE], optional = false)
    @JoinColumn(name = "tilskuddsgrunnlag_id")
    val tilskuddsgrunnlag: Tilskuddsgrunnlag,
    val tidligereUtbetalt: Int = 0
) {
    @Id
    val id = ulid()

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
    var sumUtbetaltVarig: Int = 0
    var harFerietrekkForSammeMåned: Boolean = false

    @OneToOne(orphanRemoval = true, cascade = [CascadeType.ALL])
    var beregning: Beregning? = null

    fun oppgiInntektsgrunnlag(
        inntektsgrunnlag: Inntektsgrunnlag,
        gjeldendeInntektsgrunnlag: Inntektsgrunnlag?
    ) {
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

    fun oppgiForrigeRefusjonsbeløp(forrigeRefusjonMinusBeløp: Int) {
        this.forrigeRefusjonMinusBeløp = forrigeRefusjonMinusBeløp
    }

    fun oppgiBedriftKontonummer(bedriftKontonummer: String?) {
        this.bedriftKontonummer = bedriftKontonummer
        this.bedriftKontonummerInnhentetTidspunkt = Now.localDateTime()
    }

    fun resetEndreBruttolønn() {
        this.inntekterKunFraTiltaket = null
        this.endretBruttoLønn = null
        this.fratrekkRefunderbarBeløp = null
        this.refunderbarBeløp = null
        this.beregning = null
    }

    fun endreBruttolønn(inntekterKunFraTiltaket: Boolean?, bruttoLønn: Int?) {
        if (inntekterKunFraTiltaket != null && inntekterKunFraTiltaket == true && bruttoLønn != null) {
            throw FeilkodeException(Feilkode.INNTEKTER_KUN_FRA_TILTAK_OG_OPPGIR_BELØP)
        }
        this.inntekterKunFraTiltaket = inntekterKunFraTiltaket
        this.endretBruttoLønn = bruttoLønn
    }

    fun refusjonsgrunnlagetErPositivt(): Boolean {
        return this.beregning?.refusjonsbeløp != null && this.beregning!!.refusjonsbeløp > 0
    }

    fun refusjonsgrunnlagetErNullSomIZero(): Boolean {
        return this.beregning?.refusjonsbeløp != null && this.beregning!!.refusjonsbeløp == 0
    }

    fun setInntektslinjeTilOpptjentIPeriode(inntekslinjeId: String, erOpptjentIPeriode: Boolean) {
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
        }
    }

    fun settFratrekkRefunderbarBeløp(fratrekkRefunderbarBeløp: Boolean, refunderbarBeløp: Int?) {
        if (!fratrekkRefunderbarBeløp && refunderbarBeløp != null) {
            throw FeilkodeException(Feilkode.INNTEKTER_KUN_FRA_TILTAK_OG_OPPGIR_BELØP)
        }
        this.fratrekkRefunderbarBeløp = fratrekkRefunderbarBeløp
        this.refunderbarBeløp = refunderbarBeløp
    }
}
