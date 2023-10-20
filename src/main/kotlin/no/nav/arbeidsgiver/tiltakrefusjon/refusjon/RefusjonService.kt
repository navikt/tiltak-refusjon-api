package no.nav.arbeidsgiver.tiltakrefusjon.refusjon


import io.micrometer.observation.annotation.Observed
import no.nav.arbeidsgiver.tiltakrefusjon.Feilkode
import no.nav.arbeidsgiver.tiltakrefusjon.FeilkodeException
import no.nav.arbeidsgiver.tiltakrefusjon.autorisering.InnloggetBruker
import no.nav.arbeidsgiver.tiltakrefusjon.inntekt.InntektskomponentService
import no.nav.arbeidsgiver.tiltakrefusjon.okonomi.KontoregisterService
import no.nav.arbeidsgiver.tiltakrefusjon.tilskuddsperiode.MidlerFrigjortÅrsak
import no.nav.arbeidsgiver.tiltakrefusjon.tilskuddsperiode.TilskuddsperiodeAnnullertMelding
import no.nav.arbeidsgiver.tiltakrefusjon.tilskuddsperiode.TilskuddsperiodeForkortetMelding
import no.nav.arbeidsgiver.tiltakrefusjon.tilskuddsperiode.TilskuddsperiodeGodkjentMelding
import no.nav.arbeidsgiver.tiltakrefusjon.utils.Now
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.YearMonth

@Service
@Transactional
@Observed
class RefusjonService(
    val inntektskomponentService: InntektskomponentService,
    val refusjonRepository: RefusjonRepository,
    val korreksjonRepository: KorreksjonRepository,
    val kontoregisterService: KontoregisterService,
    val minusbelopRepository: MinusbelopRepository
) {
    val log = LoggerFactory.getLogger(javaClass)

    fun opprettRefusjon(tilskuddsperiodeGodkjentMelding: TilskuddsperiodeGodkjentMelding): Refusjon? {
        log.info("Oppretter refusjon for tilskuddsperiodeId ${tilskuddsperiodeGodkjentMelding.tilskuddsperiodeId}")

        if (refusjonRepository.findAllByRefusjonsgrunnlag_Tilskuddsgrunnlag_TilskuddsperiodeId(
                tilskuddsperiodeGodkjentMelding.tilskuddsperiodeId
            )
                .isNotEmpty()
        ) {
            log.warn("Refusjon finnes allerede for tilskuddsperiode med id ${tilskuddsperiodeGodkjentMelding.tilskuddsperiodeId}")
            return null
        }

        val tilskuddsgrunnlag = Tilskuddsgrunnlag(
            avtaleId = tilskuddsperiodeGodkjentMelding.avtaleId,
            avtaleFom = tilskuddsperiodeGodkjentMelding.avtaleFom,
            avtaleTom = tilskuddsperiodeGodkjentMelding.avtaleTom,
            tilskuddsperiodeId = tilskuddsperiodeGodkjentMelding.tilskuddsperiodeId,
            deltakerFornavn = tilskuddsperiodeGodkjentMelding.deltakerFornavn,
            deltakerEtternavn = tilskuddsperiodeGodkjentMelding.deltakerEtternavn,
            deltakerFnr = tilskuddsperiodeGodkjentMelding.deltakerFnr,
            arbeidsgiverFornavn = tilskuddsperiodeGodkjentMelding.arbeidsgiverFornavn,
            arbeidsgiverEtternavn = tilskuddsperiodeGodkjentMelding.arbeidsgiverEtternavn,
            arbeidsgiverTlf = tilskuddsperiodeGodkjentMelding.arbeidsgiverTlf,
            veilederNavIdent = tilskuddsperiodeGodkjentMelding.veilederNavIdent,
            bedriftNavn = tilskuddsperiodeGodkjentMelding.bedriftNavn,
            bedriftNr = tilskuddsperiodeGodkjentMelding.bedriftNr,
            tilskuddFom = tilskuddsperiodeGodkjentMelding.tilskuddFom,
            tilskuddTom = tilskuddsperiodeGodkjentMelding.tilskuddTom,
            feriepengerSats = tilskuddsperiodeGodkjentMelding.feriepengerSats,
            otpSats = tilskuddsperiodeGodkjentMelding.otpSats,
            arbeidsgiveravgiftSats = tilskuddsperiodeGodkjentMelding.arbeidsgiveravgiftSats,
            tiltakstype = tilskuddsperiodeGodkjentMelding.tiltakstype,
            tilskuddsbeløp = tilskuddsperiodeGodkjentMelding.tilskuddsbeløp,
            lønnstilskuddsprosent = tilskuddsperiodeGodkjentMelding.lønnstilskuddsprosent,
            avtaleNr = tilskuddsperiodeGodkjentMelding.avtaleNr,
            løpenummer = tilskuddsperiodeGodkjentMelding.løpenummer,
            resendingsnummer = tilskuddsperiodeGodkjentMelding.resendingsnummer,
            enhet = tilskuddsperiodeGodkjentMelding.enhet,
            godkjentAvBeslutterTidspunkt = tilskuddsperiodeGodkjentMelding.godkjentTidspunkt
        )
        val refusjon = Refusjon(
            tilskuddsgrunnlag = tilskuddsgrunnlag,
            deltakerFnr = tilskuddsperiodeGodkjentMelding.deltakerFnr,
            bedriftNr = tilskuddsperiodeGodkjentMelding.bedriftNr
        )

        return refusjonRepository.save(refusjon)
    }

    /**
     * Avklart med resultatseksjonen og virkemiddelseksjonen at ved minusbeløp en måned vil det ikke bli noen refusjon den måneden,
     * men at vi overfører minusbeløp til neste måned dersom tiltaket fortsetter måneden etter. Hvis tiltaket avsluttes den samme måneden hvor det går i minus,
     * så går refusjonen bare i 0,-.
     */
    fun settMinusBeløpFraTidligereRefusjonerTilknyttetAvtalen(refusjon: Refusjon) {

        val avtaleNr = refusjon.refusjonsgrunnlag.tilskuddsgrunnlag.avtaleNr
        val alleMinusbeløp = minusbelopRepository.findAllByAvtaleNr(avtaleNr = avtaleNr)
        if(!alleMinusbeløp.isNullOrEmpty()) {
            val sumMinusbelop = alleMinusbeløp
                .filter { !it.gjortOpp }
                .map { minusbelop -> minusbelop.beløp}
                .filterNotNull()
                .reduceOrNull{sum, beløp -> sum + beløp}
            if (sumMinusbelop != null) {
                refusjon.refusjonsgrunnlag.oppgiForrigeRefusjonsbeløp(sumMinusbelop)
                refusjonRepository.save(refusjon)
            } else {
                refusjon.refusjonsgrunnlag.oppgiForrigeRefusjonsbeløp(0)
                refusjonRepository.save(refusjon)
            }
        }
    }

    fun settTotalBeløpUtbetalteVarigLønnstilskudd(refusjon: Refusjon) {
        if(refusjon.status == RefusjonStatus.KLAR_FOR_INNSENDING && refusjon.refusjonsgrunnlag.tilskuddsgrunnlag.tiltakstype == Tiltakstype.VARIG_LONNSTILSKUDD) {
            val alleUtbetalteVarige =
                refusjonRepository.findAllByDeltakerFnrAndBedriftNrAndStatusInAndRefusjonsgrunnlag_Tilskuddsgrunnlag_Tiltakstype(
                    refusjon.deltakerFnr,
                    refusjon.bedriftNr,
                    listOf(RefusjonStatus.UTBETALT, RefusjonStatus.SENDT_KRAV),
                    Tiltakstype.VARIG_LONNSTILSKUDD
                ).filter { it.refusjonsgrunnlag.tilskuddsgrunnlag.tilskuddFom.year.equals(Now.localDate().year) };
            val sumUtbetalt = alleUtbetalteVarige
                .map {refusjon -> refusjon.refusjonsgrunnlag.beregning?.refusjonsbeløp ?: 0 }
                .reduceOrNull{sum, beløp -> sum + beløp}
            if (sumUtbetalt != null) {
                refusjon.refusjonsgrunnlag.sumUtbetaltVarig = sumUtbetalt
            }
        }

    }

    fun gjørInntektsoppslag(utførtAv: InnloggetBruker, refusjon: Refusjon) {
        if (!refusjon.skalGjøreInntektsoppslag()) {
            return
        }

        var antallEkstraMånederSomSkalSjekkes: Int = 0

        if (refusjon.hentInntekterLengerFrem != null) {
            antallEkstraMånederSomSkalSjekkes = 1
        }
        if (refusjon.unntakOmInntekterFremitid > 0) {
            antallEkstraMånederSomSkalSjekkes = refusjon.unntakOmInntekterFremitid
        } else if (refusjon.refusjonsgrunnlag.tilskuddsgrunnlag.tiltakstype === Tiltakstype.SOMMERJOBB) {
            antallEkstraMånederSomSkalSjekkes = 1
        }
        try {
            val inntektsoppslag = inntektskomponentService.hentInntekter(
                fnr = refusjon.deltakerFnr,
                bedriftnummerDetSøkesPå = refusjon.bedriftNr,
                datoFra = refusjon.refusjonsgrunnlag.tilskuddsgrunnlag.tilskuddFom,
                datoTil = refusjon.refusjonsgrunnlag.tilskuddsgrunnlag.tilskuddTom.plusMonths(antallEkstraMånederSomSkalSjekkes.toLong())
            )
            val inntektsgrunnlag = Inntektsgrunnlag(
                inntekter = inntektsoppslag.first,
                respons = inntektsoppslag.second
            )
            refusjon.oppgiInntektsgrunnlag(utførtAv, inntektsgrunnlag, refusjon.refusjonsgrunnlag.inntektsgrunnlag)
            refusjonRepository.save(refusjon)
        } catch (e: Exception) {
            log.error("Feil ved henting av inntekter for refusjon ${refusjon.id}", e)
        }
    }

    fun godkjennForArbeidsgiver(refusjon: Refusjon, utførtAv: InnloggetBruker) {
        val alleMinusBeløp = minusbelopRepository.findAllByAvtaleNr(refusjon.refusjonsgrunnlag.tilskuddsgrunnlag.avtaleNr)
        val sumMinusbelop = alleMinusBeløp
            .filter { !it.gjortOpp }
            .map { minusbelop -> minusbelop.beløp}
            .filterNotNull()
            .reduceOrNull{sum, beløp -> sum + beløp}
        // Om det er et gammelt minusbeløp, men alle minusbeløp er gjrot opp må refusjonen lastes på ny for å reberegnes
        if (sumMinusbelop != null && sumMinusbelop != 0 && refusjon.refusjonsgrunnlag.forrigeRefusjonMinusBeløp != sumMinusbelop) {
            log.info("Arbeidsgiver prøver sende inn en refusjon hvor minusbeløp er gjort opp/endret av annen refusjon $refusjon.id")
            throw FeilkodeException(Feilkode.LAST_REFUSJONEN_PÅ_NYTT_REFUSJONSGRUNNLAG_ENDRET)
        }
        sjekkForTrukketFerietrekkForSammeMåned(refusjon)
        refusjon.godkjennForArbeidsgiver(utførtAv)
        if(refusjon.status == RefusjonStatus.GODKJENT_MINUSBELØP) {
            alleMinusBeløp.forEach {
                if (!it.gjortOpp) {
                    it.gjortOpp = true
                    it.gjortOppAvRefusjonId = refusjon.id
                    minusbelopRepository.save(it)
                }
            }
            val minusbelop = Minusbelop (
                avtaleNr = refusjon.refusjonsgrunnlag.tilskuddsgrunnlag.avtaleNr,
                beløp = refusjon.refusjonsgrunnlag.beregning?.refusjonsbeløp,
                løpenummer = refusjon.refusjonsgrunnlag.tilskuddsgrunnlag.løpenummer)
            refusjon.minusbelop = minusbelop
            log.info("Setter minusbeløp ${minusbelop.id} på refusjon ${refusjon.id}")
        }
        refusjonRepository.save(refusjon)
    }

    fun godkjennNullbeløpForArbeidsgiver(refusjon: Refusjon, utførtAv: InnloggetBruker) {
        refusjon.godkjennNullbeløpForArbeidsgiver(utførtAv)
        refusjonRepository.save(refusjon)
    }

    fun annullerRefusjon(melding: TilskuddsperiodeAnnullertMelding) {
        log.info("Annullerer refusjon med tilskuddsperiodeId ${melding.tilskuddsperiodeId}")
        refusjonRepository.findAllByRefusjonsgrunnlag_Tilskuddsgrunnlag_TilskuddsperiodeId(melding.tilskuddsperiodeId)
            .firstOrNull()
            ?.let {
                if (melding.årsak == MidlerFrigjortÅrsak.AVTALE_ANNULLERT) {
                    it.annuller()
                } else {
                    log.info("Grunn for annullering av tilskuddsperiode er ${melding.årsak}, annullerer ikke refusjon.")
                }
                it.midlerFrigjortÅrsak = melding.årsak
                refusjonRepository.save(it)
            }
    }

    fun forkortRefusjon(melding: TilskuddsperiodeForkortetMelding) {
        log.info("Forkorter refusjon med tilskuddsperiodeId ${melding.tilskuddsperiodeId} til dato ${melding.tilskuddTom} med nytt beløp ${melding.tilskuddsbeløp}")
        refusjonRepository.findAllByRefusjonsgrunnlag_Tilskuddsgrunnlag_TilskuddsperiodeId(melding.tilskuddsperiodeId)
            .firstOrNull()
            ?.let {
                it.forkort(melding.tilskuddTom, melding.tilskuddsbeløp)
                refusjonRepository.save(it)
            } ?: run {
            log.warn("Kunne ikke forkorte refusjon med tilskuddsperiodeId ${melding.tilskuddsperiodeId}, fant ikke refusjonen")
        }
    }

    fun gjørBedriftKontonummeroppslag(refusjon: Refusjon) {
        if (!refusjon.skalGjøreKontonummerOppslag()) {
            return
        }
        refusjon.oppgiBedriftKontonummer(kontoregisterService.hentBankkontonummer(refusjon.bedriftNr))

        refusjonRepository.save(refusjon)
    }

    fun opprettKorreksjonsutkast(refusjon: Refusjon, korreksjonsgrunner: Set<Korreksjonsgrunn>, unntakOmInntekterFremitid: Int?, annetGrunn: String?): Korreksjon {
        val korreksjonsutkast = refusjon.opprettKorreksjonsutkast(korreksjonsgrunner, unntakOmInntekterFremitid, annetGrunn)
        korreksjonRepository.save(korreksjonsutkast)
        refusjonRepository.save(refusjon)
        return korreksjonsutkast
    }

    fun sjekkForTrukketFerietrekkForSammeMåned(refusjon: Refusjon) {
        if (refusjon.refusjonsgrunnlag.beregning?.fratrekkLønnFerie == 0) {
            return
        }
        val statuser = listOf(RefusjonStatus.UTBETALT, RefusjonStatus.SENDT_KRAV, RefusjonStatus.GODKJENT_MINUSBELØP, RefusjonStatus.GODKJENT_NULLBELØP)
        refusjonRepository.findAllByRefusjonsgrunnlag_Tilskuddsgrunnlag_AvtaleNrAndStatusIn(refusjon.refusjonsgrunnlag.tilskuddsgrunnlag.avtaleNr, statuser)
            .filter { YearMonth.from(it.refusjonsgrunnlag.tilskuddsgrunnlag.tilskuddFom)  == YearMonth.from(refusjon.refusjonsgrunnlag.tilskuddsgrunnlag.tilskuddFom) }
            .forEach {
                if (it.refusjonsgrunnlag.beregning?.fratrekkLønnFerie != 0) {
                    log.info("Forsøkte å godkjenne refusjon ${it.id} med ferietrekk. Det er allerde trukket for ferie i en refusjon i samme måned: ${refusjon.id}")
                    throw FeilkodeException(Feilkode.FERIETREKK_TRUKKET_FOR_SAMME_MÅNED)
                }
            }
    }

    fun settOmFerieErTrukketForSammeMåned(refusjon: Refusjon) {
        if (refusjon.status == RefusjonStatus.KLAR_FOR_INNSENDING) {
            val statuser = listOf(RefusjonStatus.UTBETALT, RefusjonStatus.SENDT_KRAV, RefusjonStatus.GODKJENT_MINUSBELØP, RefusjonStatus.GODKJENT_NULLBELØP)
            refusjonRepository.findAllByRefusjonsgrunnlag_Tilskuddsgrunnlag_AvtaleNrAndStatusIn(refusjon.refusjonsgrunnlag.tilskuddsgrunnlag.avtaleNr, statuser)
                .filter { YearMonth.from(it.refusjonsgrunnlag.tilskuddsgrunnlag.tilskuddFom)  == YearMonth.from(refusjon.refusjonsgrunnlag.tilskuddsgrunnlag.tilskuddFom) }
                .forEach {
                    if (it.refusjonsgrunnlag.beregning?.fratrekkLønnFerie != 0 && !refusjon.refusjonsgrunnlag.harFerietrekkForSammeMåned) {
                        log.info("Ferietrekk er trukket på en tidligere refusjon: ${it.id} på samme avtalenr i samme måned på denne refusjonen: ${refusjon.id} setter harFerieTrekkForSammeMåned til true")
                        refusjon.refusjonsgrunnlag.harFerietrekkForSammeMåned = true
                    }
                }
            refusjonRepository.save(refusjon)
        }
    }

}