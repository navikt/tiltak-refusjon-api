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
import kotlin.streams.toList

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

    @OneToOne(orphanRemoval = true, cascade = [CascadeType.ALL])
    var beregning: Beregning? = null

    fun oppgiInntektsgrunnlag(inntektsgrunnlag: Inntektsgrunnlag): Boolean {
        this.inntektsgrunnlag = inntektsgrunnlag
        return gjørBeregning()
    }

    fun oppgiBedriftKontonummer(bedrifKontonummer: String?): Boolean {
        this.bedriftKontonummer = bedrifKontonummer
        this.bedriftKontonummerInnhentetTidspunkt = Now.localDateTime()
        return gjørBeregning()
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
        return bedriftKontonummer != null && (inntekterKunFraTiltaket == true && endretBruttoLønn == null || ((inntekterKunFraTiltaket == false || inntekterKunFraTiltaket == null) && endretBruttoLønn != null))
    }

    private fun gjørBeregning(): Boolean {
        if (erAltOppgitt()) {
            this.beregning = beregnRefusjonsbeløp(
                inntekter = inntektsgrunnlag!!.inntekter.filter { it.skalRefunderes }.toList(),
                tilskuddsgrunnlag = tilskuddsgrunnlag,
                tidligereUtbetalt = tidligereUtbetalt,
                korrigertBruttoLønn = endretBruttoLønn
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

        val månederTilskudd =  tilskuddFom.datesUntil(tilskuddTom).map { YearMonth.of(it.year, it.month) }.distinct().toList()

        return månederInntekter.containsAll(månederTilskudd)
    }

    fun toggleInntektslinje(inntekslinjeId: String): Boolean {
        inntektsgrunnlag?.inntekter?.filter { it.id == inntekslinjeId }?.forEach {
            it.skalRefunderes  = !it.skalRefunderes
        }
        return gjørBeregning()
    }
}
