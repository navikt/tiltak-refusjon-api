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
    var id = ULID.random()

    @OneToOne(orphanRemoval = true, cascade = [CascadeType.ALL])
    var inntektsgrunnlag: Inntektsgrunnlag? = null
    var bedriftKontonummer: String? = null
    var bedriftKontonummerInnhentetTidspunkt: LocalDateTime? = null
    var inntekterKunFraTiltaket: Boolean? = null
    var endretBruttoLønn: Int? = null
    @OneToOne(orphanRemoval = true, cascade = [CascadeType.ALL])
    var beregning: Beregning? = null

    fun oppgiInntektsgrunnlag(inntektsgrunnlag: Inntektsgrunnlag) {
        this.inntektsgrunnlag = inntektsgrunnlag
        gjørBeregning()
    }

    fun oppgiBedriftKontonummer(bedrifKontonummer: String) {
        this.bedriftKontonummer = bedrifKontonummer
        this.bedriftKontonummerInnhentetTidspunkt = Now.localDateTime()
        gjørBeregning()
    }

    fun endreBruttolønn(inntekterKunFraTiltaket: Boolean, bruttoLønn: Int?) {
        if (inntekterKunFraTiltaket && bruttoLønn != null) {
            throw FeilkodeException(Feilkode.INNTEKTER_KUN_FRA_TILTAK_OG_OPPGIR_BELØP)
        }
        this.inntekterKunFraTiltaket = inntekterKunFraTiltaket
        this.endretBruttoLønn = bruttoLønn
        gjørBeregning()
    }

    fun erAltOppgitt(): Boolean {
        val inntektsgrunnlag = inntektsgrunnlag
        if (inntektsgrunnlag == null || inntektsgrunnlag.inntekter.none() { it.erMedIInntektsgrunnlag() }) return false
        return bedriftKontonummer != null && (inntekterKunFraTiltaket == true && endretBruttoLønn == null || inntekterKunFraTiltaket == false && endretBruttoLønn != null)
    }

    fun gjørBeregning() {
        if (erAltOppgitt()) {
            this.beregning = beregnRefusjonsbeløp(
                inntekter = inntektsgrunnlag!!.inntekter.toList(),
                tilskuddsgrunnlag = tilskuddsgrunnlag,
                appImageId = "appImageId",
                tidligereUtbetalt = tidligereUtbetalt,
                korrigertBruttoLønn = endretBruttoLønn
            )
        }
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
}
