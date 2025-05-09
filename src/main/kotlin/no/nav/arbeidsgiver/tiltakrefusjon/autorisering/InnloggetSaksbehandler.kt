package no.nav.arbeidsgiver.tiltakrefusjon.autorisering

import com.fasterxml.jackson.annotation.JsonIgnore
import no.nav.arbeidsgiver.tiltakrefusjon.RessursFinnesIkkeException
import no.nav.arbeidsgiver.tiltakrefusjon.inntekt.InntektskomponentService
import no.nav.arbeidsgiver.tiltakrefusjon.norg.NorgService
import no.nav.arbeidsgiver.tiltakrefusjon.okonomi.KontoregisterService
import no.nav.arbeidsgiver.tiltakrefusjon.persondata.PersondataService
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.BegrensetRefusjon
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.Beregning
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.BrukerRolle
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.HentSaksbehandlerRefusjonerQueryParametre
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.Inntektsgrunnlag
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.Korreksjon
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.KorreksjonRepository
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.Korreksjonsgrunn
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.Refusjon
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.RefusjonRepository
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.RefusjonService
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.RefusjonStatus
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.Tiltakstype
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.beregnRefusjonsbeløp
import no.nav.team_tiltak.felles.persondata.pdl.domene.Diskresjonskode
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.repository.findByIdOrNull
import java.time.LocalDate
import java.util.*

data class InnloggetSaksbehandler(
    override val identifikator: String,
    val azureOid: UUID,
    val navn: String,
    @JsonIgnore val tilgangskontrollService: TilgangskontrollService,
    @JsonIgnore val norgeService: NorgService,
    @JsonIgnore val refusjonRepository: RefusjonRepository,
    @JsonIgnore val korreksjonRepository: KorreksjonRepository,
    @JsonIgnore val refusjonService: RefusjonService,
    @JsonIgnore val inntektskomponentService: InntektskomponentService,
    @JsonIgnore val kontoregisterService: KontoregisterService,
    @JsonIgnore val adGruppeTilganger: AdGruppeTilganger,
    @JsonIgnore val persondataService: PersondataService,
) : InnloggetBruker {
    @JsonIgnore
    val log: Logger = LoggerFactory.getLogger(javaClass)
    val internIdentifikatorer = InternIdentifikatorer(identifikator, azureOid)
    val harKorreksjonTilgang = adGruppeTilganger.korreksjon
    override val rolle: BrukerRolle = BrukerRolle.BESLUTTER

    fun finnAlle(queryParametre: HentSaksbehandlerRefusjonerQueryParametre): Map<String, Any> {
        val pageable: Pageable = PageRequest.of(queryParametre.page, queryParametre.size, Sort.Direction.ASC, "fristForGodkjenning", "status", "id")

        val statuser = if (queryParametre.status != null) listOf(queryParametre.status) else RefusjonStatus.values().toList()
        val tiltakstyper = if (queryParametre.tiltakstype != null) listOf(queryParametre.tiltakstype) else Tiltakstype.values().toList()

        val reusjonPage: Page<Refusjon> =
            if (!queryParametre.veilederNavIdent.isNullOrBlank()) {
                refusjonRepository.findAllByRefusjonsgrunnlag_Tilskuddsgrunnlag_VeilederNavIdentAndStatusInAndRefusjonsgrunnlag_Tilskuddsgrunnlag_TiltakstypeIn(
                    queryParametre.veilederNavIdent,
                    statuser,
                    tiltakstyper,
                    pageable
                )
            } else if (!queryParametre.deltakerFnr.isNullOrBlank()) {
                refusjonRepository.findAllByDeltakerFnrAndStatusInAndRefusjonsgrunnlag_Tilskuddsgrunnlag_TiltakstypeIn(
                    queryParametre.deltakerFnr,
                    statuser,
                    tiltakstyper,
                    pageable
                )
            } else if (!queryParametre.bedriftNr.isNullOrBlank()) {
                refusjonRepository.findAllByBedriftNrAndStatusInAndRefusjonsgrunnlag_Tilskuddsgrunnlag_TiltakstypeIn(
                    queryParametre.bedriftNr,
                    statuser,
                    tiltakstyper,
                    pageable
                )
            } else if (!queryParametre.enhet.isNullOrBlank()) {
                refusjonRepository.findAllByRefusjonsgrunnlag_Tilskuddsgrunnlag_EnhetAndStatusInAndRefusjonsgrunnlag_Tilskuddsgrunnlag_TiltakstypeIn(
                    queryParametre.enhet,
                    statuser,
                    tiltakstyper,
                    pageable
                )
            } else if (queryParametre.avtaleNr != null) {
                refusjonRepository.findAllByRefusjonsgrunnlag_Tilskuddsgrunnlag_AvtaleNrAndStatusInAndRefusjonsgrunnlag_Tilskuddsgrunnlag_TiltakstypeIn(
                    queryParametre.avtaleNr!!,
                    statuser,
                    tiltakstyper,
                    pageable
                )
            } else {
                PageImpl(emptyList())
            }

        val diskresjonskoder = hentDiskresjonskoder(reusjonPage.content)
        val refusjonerMedTilgang = reusjonPage.content
            .filter { tilgangskontrollService.harLeseTilgang(internIdentifikatorer, it.deltakerFnr) }
            .map { BegrensetRefusjon.fraRefusjon(it, diskresjonskoder[it.deltakerFnr]) }

        return mapOf(
            Pair("refusjoner", refusjonerMedTilgang),
            Pair("size", reusjonPage.size),
            Pair("currentPage", reusjonPage.number),
            Pair("totalItems", reusjonPage.totalElements),
            Pair("totalPages", reusjonPage.totalPages)
        )
    }

    fun hentDiskresjonskoder(refusjoner: List<Refusjon>): Map<String, Diskresjonskode> {
        if (adGruppeTilganger.fortroligAdresse || adGruppeTilganger.strengtFortroligAdresse) {
            return persondataService.hentDiskresjonskoder(refusjoner.map { it.deltakerFnr }.toSet())
        }
        return emptyMap()
    }

    fun finnRefusjon(id: String): Refusjon {
        val refusjon = refusjonRepository.findByIdOrNull(id) ?: throw RessursFinnesIkkeException()
        sjekkLesetilgang(refusjon)
        return refusjon
    }

    fun finnKorreksjon(id: String): Korreksjon {
        val korreksjon = korreksjonRepository.findByIdOrNull(id) ?: throw RessursFinnesIkkeException()
        sjekkLesetilgang(korreksjon)
        if (korreksjon.skalGjøreKontonummerOppslag()) {
            val kontonummer = kontoregisterService.hentBankkontonummer(korreksjon.bedriftNr)
            korreksjon.refusjonsgrunnlag.oppgiBedriftKontonummer(kontonummer)
            korreksjonRepository.save(korreksjon)
        }
        if (korreksjon.skalGjøreInntektsoppslag()) {
            var antallMånederSomSkalSjekkes: Long = 1
            if (korreksjon.korreksjonsgrunner.contains(Korreksjonsgrunn.HENT_INNTEKTER_TO_MÅNEDER_FREM)) {
                if (korreksjon.unntakOmInntekterFremitid != null) {
                    antallMånederSomSkalSjekkes = korreksjon.unntakOmInntekterFremitid.toLong()
                } else {
                    antallMånederSomSkalSjekkes = 2
                }
            }

            val inntektsoppslag = inntektskomponentService.hentInntekter(
                fnr = korreksjon.deltakerFnr,
                bedriftnummerDetSøkesPå = korreksjon.bedriftNr,
                datoFra = korreksjon.refusjonsgrunnlag.tilskuddsgrunnlag.tilskuddFom,
                datoTil = korreksjon.refusjonsgrunnlag.tilskuddsgrunnlag.tilskuddTom.plusMonths(antallMånederSomSkalSjekkes)
            )
            val inntektsgrunnlag = Inntektsgrunnlag(
                inntekter = inntektsoppslag.first,
                respons = inntektsoppslag.second
            )
            korreksjon.oppgiInntektsgrunnlag(inntektsgrunnlag)
            refusjonService.gjørKorreksjonBeregning(korreksjon, this)
            korreksjonRepository.save(korreksjon)
        }
        return korreksjon
    }

    private fun sjekkLesetilgang(refusjon: Refusjon) {
        if (!tilgangskontrollService.harLeseTilgang(internIdentifikatorer, refusjon.deltakerFnr)) {
            throw TilgangskontrollException()
        }
    }

    private fun sjekkLesetilgang(korreksjon: Korreksjon) {
        if (!tilgangskontrollService.harLeseTilgang(internIdentifikatorer, korreksjon.deltakerFnr)) {
            throw TilgangskontrollException()
        }
    }

    private fun sjekkKorreksjonTilgang() {
        if (!harKorreksjonTilgang) {
            throw TilgangskontrollException()
        }
    }

    fun opprettKorreksjonsutkast(id: String, korreksjonsgrunner: Set<Korreksjonsgrunn>, unntakOmInntekterFremitid: Int?, annetGrunn: String?): Refusjon {
        sjekkKorreksjonTilgang()
        val refusjonSomSkalKorrigeres = finnRefusjon(id)
        log.info("Oppretter korreksjonsutkast fra refusjon med id ${refusjonSomSkalKorrigeres.id} og status ${refusjonSomSkalKorrigeres.status}")
        refusjonService.opprettKorreksjonsutkast(refusjonSomSkalKorrigeres, korreksjonsgrunner, unntakOmInntekterFremitid, annetGrunn)
        return refusjonSomSkalKorrigeres
    }

    fun slettKorreksjonsutkast(id: String) {
        sjekkKorreksjonTilgang()
        val korreksjon = finnKorreksjon(id)
        sjekkLesetilgang(korreksjon)
        val refusjon = finnRefusjon(korreksjon.korrigererRefusjonId)
        sjekkLesetilgang(refusjon)
        if (korreksjon.kanSlettes()) {
            refusjon.fjernKorreksjonId()
            refusjonRepository.save(refusjon)
            korreksjonRepository.delete(korreksjon)
        }
    }

    fun utbetalKorreksjon(id: String) {
        sjekkKorreksjonTilgang()
        val korreksjon = korreksjonRepository.findByIdOrNull(id) ?: throw RessursFinnesIkkeException()
        sjekkLesetilgang(korreksjon)
        val refusjon = finnRefusjon(korreksjon.korrigererRefusjonId)
        sjekkLesetilgang(refusjon)

        korreksjon.utbetalKorreksjon(this, korreksjon.refusjonsgrunnlag.tilskuddsgrunnlag.enhet ?: "")
        refusjon.status = RefusjonStatus.KORRIGERT

        refusjonRepository.save(refusjon)
        korreksjonRepository.save(korreksjon)
    }

    fun fullførKorreksjonVedOppgjort(id: String) {
        sjekkKorreksjonTilgang()
        val korreksjon = korreksjonRepository.findByIdOrNull(id) ?: throw RessursFinnesIkkeException()
        sjekkLesetilgang(korreksjon)
        val refusjon = finnRefusjon(korreksjon.korrigererRefusjonId)
        sjekkLesetilgang(refusjon)

        korreksjon.fullførKorreksjonVedOppgjort(this)
        refusjon.status = RefusjonStatus.KORRIGERT

        refusjonRepository.save(refusjon)
        korreksjonRepository.save(korreksjon)
    }

    fun fullførKorreksjonVedTilbakekreving(id: String) {
        sjekkKorreksjonTilgang()
        val korreksjon = korreksjonRepository.findByIdOrNull(id) ?: throw RessursFinnesIkkeException()
        sjekkLesetilgang(korreksjon)
        val refusjon = finnRefusjon(korreksjon.korrigererRefusjonId)
        sjekkLesetilgang(refusjon)

        korreksjon.fullførKorreksjonVedTilbakekreving(this)
        refusjon.status = RefusjonStatus.KORRIGERT

        refusjonRepository.save(refusjon)
        korreksjonRepository.save(korreksjon)
    }

    fun endreBruttolønn(id: String, inntekterKunFraTiltaket: Boolean?, endretBruttoLønn: Int?) {
        sjekkKorreksjonTilgang()
        val korreksjon = korreksjonRepository.findByIdOrNull(id) ?: throw RessursFinnesIkkeException()
        sjekkLesetilgang(korreksjon)
        korreksjon.endreBruttolønn(inntekterKunFraTiltaket, endretBruttoLønn)
        refusjonService.gjørKorreksjonBeregning(korreksjon, this)

        korreksjonRepository.save(korreksjon)
    }

    fun forlengFrist(id: String, nyFrist: LocalDate, årsak: String): Refusjon {
        val refusjon = finnRefusjon(id)
        refusjon.forlengFrist(nyFrist, årsak, this)
        refusjonRepository.save(refusjon)
        return refusjon
    }

    fun merkForUnntakOmInntekterFremITid(id: String, merking: Int) {
        val refusjon = finnRefusjon(id)
        refusjon.merkForUnntakOmInntekterFremITid(merking, this)
        refusjonRepository.save(refusjon)
    }

    fun setInntektslinjeTilOpptjentIPeriode(korreksjonId: String, inntekslinjeId: String, erOpptjentIPeriode: Boolean) {
        sjekkKorreksjonTilgang()
        val korreksjon = korreksjonRepository.findByIdOrNull(korreksjonId) ?: throw RessursFinnesIkkeException()
        sjekkLesetilgang(korreksjon)
        korreksjon.setInntektslinjeTilOpptjentIPeriode(inntekslinjeId, erOpptjentIPeriode)
        refusjonService.gjørKorreksjonBeregning(korreksjon, this)

        korreksjonRepository.save(korreksjon)
    }

    fun settFratrekkRefunderbarBeløp(id: String, fratrekkRefunderbarBeløp: Boolean, refunderbarBeløp: Int?) {
        val korreksjon: Korreksjon = korreksjonRepository.findByIdOrNull(id) ?: throw RessursFinnesIkkeException()
        sjekkLesetilgang(korreksjon)
        korreksjon.settFratrekkRefunderbarBeløp(fratrekkRefunderbarBeløp, refunderbarBeløp)
        refusjonService.gjørKorreksjonBeregning(korreksjon, this)

        korreksjonRepository.save(korreksjon)
    }

    fun overstyrMinusbeløp(id: String, minusBeløp: Int) {
        val korreksjon: Korreksjon = korreksjonRepository.findByIdOrNull(id) ?: throw RessursFinnesIkkeException()
        sjekkLesetilgang(korreksjon)
        korreksjon.refusjonsgrunnlag.forrigeRefusjonMinusBeløp = minusBeløp
        refusjonService.gjørKorreksjonBeregning(korreksjon, this);
    }

    fun overstyrHarFerietrekkForSammeMåned(id: String, harFerietrekkForSammeMåned: Boolean) {
        val korreksjon: Korreksjon = korreksjonRepository.findByIdOrNull(id) ?: throw RessursFinnesIkkeException()
        sjekkLesetilgang(korreksjon)
        korreksjon.refusjonsgrunnlag.harFerietrekkForSammeMåned = harFerietrekkForSammeMåned
        refusjonService.gjørKorreksjonBeregning(korreksjon, this);
    }

    fun hentEnhet(enhet: String): String? {
        return norgeService.hentEnhetNavn(enhet)
    }

    fun reberegnDryRun(id: String, harFerietrekkForSammeMåned: Boolean, minusBeløp: Int): Beregning {
        val refusjon = finnRefusjon(id)
        return beregnRefusjonsbeløp(
            inntekter = refusjon.refusjonsgrunnlag.inntektsgrunnlag!!.inntekter.toList(),
            tilskuddsgrunnlag = refusjon.refusjonsgrunnlag.tilskuddsgrunnlag,
            tidligereUtbetalt = 0,
            korrigertBruttoLønn = refusjon.refusjonsgrunnlag.endretBruttoLønn,
            fratrekkRefunderbarSum = refusjon.refusjonsgrunnlag.refunderbarBeløp,
            forrigeRefusjonMinusBeløp = minusBeløp,
            tilskuddFom = refusjon.refusjonsgrunnlag.tilskuddsgrunnlag.tilskuddFom,
            harFerietrekkForSammeMåned = harFerietrekkForSammeMåned,
            sumUtbetaltVarig = refusjon.refusjonsgrunnlag.sumUtbetaltVarig
        )
    }
}
