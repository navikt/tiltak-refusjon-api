package no.nav.arbeidsgiver.tiltakrefusjon.refusjon


import io.micrometer.observation.annotation.Observed
import no.nav.arbeidsgiver.tiltakrefusjon.Feilkode
import no.nav.arbeidsgiver.tiltakrefusjon.FeilkodeException
import no.nav.arbeidsgiver.tiltakrefusjon.autorisering.InnloggetBruker
import no.nav.arbeidsgiver.tiltakrefusjon.autorisering.SYSTEM_BRUKER
import no.nav.arbeidsgiver.tiltakrefusjon.grunnbelop.GrunnbelopService
import no.nav.arbeidsgiver.tiltakrefusjon.inntekt.InntektskomponentService
import no.nav.arbeidsgiver.tiltakrefusjon.okonomi.KontoregisterService
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.events.BeregningUtført
import no.nav.arbeidsgiver.tiltakrefusjon.refusjon.events.KorreksjonBeregningUtført
import no.nav.arbeidsgiver.tiltakrefusjon.tilskuddsperiode.MidlerFrigjortÅrsak
import no.nav.arbeidsgiver.tiltakrefusjon.tilskuddsperiode.TilskuddsperiodeAnnullertMelding
import no.nav.arbeidsgiver.tiltakrefusjon.tilskuddsperiode.TilskuddsperiodeForkortetMelding
import no.nav.arbeidsgiver.tiltakrefusjon.tilskuddsperiode.TilskuddsperiodeGodkjentMelding
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.YearMonth

@Service
@Observed
class RefusjonService(
    private val inntektskomponentService: InntektskomponentService,
    private val refusjonRepository: RefusjonRepository,
    private val korreksjonRepository: KorreksjonRepository,
    private val kontoregisterService: KontoregisterService,
    private val minusbelopRepository: MinusbelopRepository,
    private val applicationEventPublisher: ApplicationEventPublisher,
    private val grunnbelopService: GrunnbelopService,
) {
    val log: Logger = LoggerFactory.getLogger(javaClass)

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
            godkjentAvBeslutterTidspunkt = tilskuddsperiodeGodkjentMelding.godkjentTidspunkt,
            mentorTimelonn = tilskuddsperiodeGodkjentMelding.mentorTimelonn,
            mentorAntallTimer = tilskuddsperiodeGodkjentMelding.mentorAntallTimer,
        )
        val refusjon = Refusjon(
            tilskuddsgrunnlag = tilskuddsgrunnlag,
            deltakerFnr = tilskuddsperiodeGodkjentMelding.deltakerFnr,
            bedriftNr = tilskuddsperiodeGodkjentMelding.bedriftNr
        )

        refusjon.refusjonsgrunnlag.bedriftKid = tilskuddsperiodeGodkjentMelding.arbeidsgiverKid

        oppdaterRefusjon(refusjon, SYSTEM_BRUKER)

        return refusjonRepository.save(refusjon)
    }

    /**
     * Avklart med resultatseksjonen og virkemiddelseksjonen at ved minusbeløp en måned vil det ikke bli noen refusjon den måneden,
     * men at vi overfører minusbeløp til neste måned dersom tiltaket fortsetter måneden etter. Hvis tiltaket avsluttes den samme måneden hvor det går i minus,
     * så går refusjonen bare i 0,-.
     */
    fun settMinusbeløpFraTidligereRefusjonerTilknyttetAvtalen(refusjon: Refusjon) {
        val avtaleNr = refusjon.refusjonsgrunnlag.tilskuddsgrunnlag.avtaleNr
        val alleMinusbeløp = minusbelopRepository.findAllByAvtaleNr(avtaleNr)
        if (alleMinusbeløp.isNotEmpty()) {
            val sumMinusbelop = alleMinusbeløp
                .filter { !it.gjortOpp }
                .mapNotNull { minusbelop -> minusbelop.beløp }
                .sum()
            refusjon.refusjonsgrunnlag.oppgiForrigeRefusjonsbeløp(sumMinusbelop)
            refusjonRepository.save(refusjon)
        }
    }

    /**
     * Refusjoner som går i minus skal ikke regnes med, feks ved beregning av "totalt utbetalt"
     */
    private val utbetalteRefusjonsstatuser: List<RefusjonStatus> = RefusjonStatus.entries.filter { it.ansesSomUtbetalt() }
    private val behandledeKorreksjoner: List<Korreksjonstype> = Korreksjonstype.entries.filter { it.isSendtInn() }

    /**
     * Forskrift sier at "refusjonen kan ikke overstige 5 ganger grunnbeløp per år". Dette har vi tolket til å bety
     * at 5g-grensen gjelder for en deltaker ansatt i en spesifikk bedrift på en spesifikk type tiltak.
     * Altså skal summen gjelde **på tvers** av avtalene som en person har i en bedrift (i normaltilfeller skal dette være bare en avtale).
     * <p>
     * Det betyr altså at hvis en deltaker har to varige lønnstilskudd hos en arbeidsgiver, så dekkes de av samme 5g-grense,
     * men ikke hvis deltakeren har varig lønnstilskudd i to forskjellige bedrifter.
     */
    fun totaltUtbetaltForTiltakMed5gBegrensning(refundering: Refundering): Int {
        val utbetalteRefusjoner =
            refusjonRepository.findAllByDeltakerFnrAndBedriftNrAndStatusInAndRefusjonsgrunnlag_Tilskuddsgrunnlag_Tiltakstype(
                refundering.deltakerFnr,
                refundering.bedriftNr,
                utbetalteRefusjonsstatuser,
                refundering.tiltakstype()
            )
        val utbetalteKorreksjoner =
            korreksjonRepository.findAllByDeltakerFnrAndBedriftNrAndStatusInAndRefusjonsgrunnlag_Tilskuddsgrunnlag_Tiltakstype(
                refundering.deltakerFnr,
                refundering.bedriftNr,
                behandledeKorreksjoner,
                refundering.tiltakstype()
            )

        val alleInnsendteRefusjonerOgKorreksjonerForTiltaket = (utbetalteRefusjoner + utbetalteKorreksjoner)

        val alleUtbetalteForSammeÅr: List<Refundering> = alleInnsendteRefusjonerOgKorreksjonerForTiltaket
            .filter { utbetaltRefundering ->
                utbetaltRefundering.fraSammeÅrSom(refundering)
            }

        val nyBeregnetSum = alleUtbetalteForSammeÅr
            .mapNotNull { it.refusjonsgrunnlag.beregning?.refusjonsbeløp }
            .sum()

        // Gammel sum ble beregnet av kun refusjoner, og status "utbetaling feilet" var ikke inkludert
        val gammelBeregnetSum = alleUtbetalteForSammeÅr
            .filterIsInstance<Refusjon>()
            .filter { it.status != RefusjonStatus.UTBETALING_FEILET }
            .mapNotNull { it.refusjonsgrunnlag.beregning?.refusjonsbeløp }
            .sum()

        if (nyBeregnetSum != gammelBeregnetSum) {
            log.warn(
                "Ny beregning for totalt utbetalt for tiltak avviker fra gammel beregning. Avtalenr: {}," +
                        "ny sum: {}, gammel sum: {}. Har feilede refusjonsutbetalinger: {}," +
                        "har korreksjoner: {}",
                refundering.refusjonsgrunnlag.tilskuddsgrunnlag.avtaleNr,
                nyBeregnetSum,
                gammelBeregnetSum,
                alleUtbetalteForSammeÅr.any { it.status == RefusjonStatus.UTBETALING_FEILET },
                alleUtbetalteForSammeÅr.any { it is Korreksjon })
        }

        return gammelBeregnetSum
    }

    fun gjørInntektsoppslag(refusjon: Refusjon, utførtAv: InnloggetBruker) {
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
            refusjon.oppgiInntektsgrunnlag(inntektsgrunnlag, refusjon.refusjonsgrunnlag.inntektsgrunnlag)
            gjørBeregning(refusjon, utførtAv)
            refusjonRepository.save(refusjon)
        } catch (e: Exception) {
            log.error("Feil ved henting av inntekter for refusjon ${refusjon.id}", e)
        }
    }

    fun godkjennForArbeidsgiver(refusjon: Refusjon, utførtAv: InnloggetBruker) {
        val alleMinusBeløp = minusbelopRepository.findAllByAvtaleNr(refusjon.refusjonsgrunnlag.tilskuddsgrunnlag.avtaleNr)
        val sumMinusbelop = alleMinusBeløp
            .filter { !it.gjortOpp }
            .mapNotNull { minusbelop -> minusbelop.beløp }
            .reduceOrNull { sum, beløp -> sum + beløp }
        // Om det er et gammelt minusbeløp, men alle minusbeløp er gjort opp må refusjonen lastes på ny for å reberegnes
        if (sumMinusbelop != null && sumMinusbelop != 0 && refusjon.refusjonsgrunnlag.forrigeRefusjonMinusBeløp != sumMinusbelop) {
            log.info("Arbeidsgiver prøver å sende inn en refusjon hvor minusbeløp er gjort opp/endret av annen refusjon ${refusjon.id}")
            throw FeilkodeException(Feilkode.LAST_REFUSJONEN_PÅ_NYTT_REFUSJONSGRUNNLAG_ENDRET)
        }
        sjekkForTrukketFerietrekkForSammeMåned(refusjon)
        refusjon.godkjennForArbeidsgiver(utførtAv)

        // Gjør opp alle eventuelle minusbeløp
        alleMinusBeløp.forEach {
            if (!it.gjortOpp) {
                it.gjortOpp = true
                it.gjortOppAvRefusjonId = refusjon.id
                minusbelopRepository.save(it)
            }
        }

        // Lag en nytt minusbeløp om refusjonen er i minus
        if (refusjon.status == RefusjonStatus.GODKJENT_MINUSBELØP) {
            val minusbelop = Minusbelop(
                avtaleNr = refusjon.refusjonsgrunnlag.tilskuddsgrunnlag.avtaleNr,
                beløp = refusjon.refusjonsgrunnlag.beregning?.refusjonsbeløp,
                løpenummer = refusjon.refusjonsgrunnlag.tilskuddsgrunnlag.løpenummer
            )
            refusjon.minusbelop = minusbelop
            log.info("Setter minusbeløp ${minusbelop.id} på refusjon ${refusjon.id}")
        }

        refusjonRepository.save(refusjon)
        // Oppdater åpne refusjoner med data (feks maksbeløp, sumUtbetalt for 5g-beregning, ferietrekk etc..)
        val alleRefusjonerSomSkalSendesInn =
            refusjonRepository.findAllByRefusjonsgrunnlag_Tilskuddsgrunnlag_AvtaleNrAndStatusIn(
                refusjon.refusjonsgrunnlag.tilskuddsgrunnlag.avtaleNr,
                RefusjonStatus.entries.filter { it.isUbehandlet() }
            )
        alleRefusjonerSomSkalSendesInn.forEach {
            if (it.id != refusjon.id) {
                oppdaterRefusjon(it, utførtAv)
                refusjonRepository.save(it)
            }
        }
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
        if (refusjon.tiltakstype() == Tiltakstype.VTAO) {
            // Utfør beregning umiddelbart for VTAO-korreksjoner
            korreksjonsutkast.refusjonsgrunnlag.beregning = beregnKorreksjon(
                Beregningskontekst(grunnbelopService.alleGrunnbelop()),
                korreksjonsutkast
            )
        }
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
            .filter { YearMonth.from(it.refusjonsgrunnlag.tilskuddsgrunnlag.tilskuddFom) == YearMonth.from(refusjon.refusjonsgrunnlag.tilskuddsgrunnlag.tilskuddFom) }
            .forEach {
                if (it.refusjonsgrunnlag.beregning?.fratrekkLønnFerie != 0) {
                    log.info("Forsøkte å godkjenne refusjon ${refusjon.id} med ferietrekk. Det er allerde trukket for ferie i en refusjon i samme måned: ${it.id}")
                    throw FeilkodeException(Feilkode.FERIETREKK_TRUKKET_FOR_SAMME_MÅNED)
                }
            }
    }

    fun settOmFerieErTrukketForSammeMåned(refusjon: Refusjon) {
        if (refusjon.status.isUbehandlet()) {
            val statuser = listOf(RefusjonStatus.UTBETALT, RefusjonStatus.SENDT_KRAV, RefusjonStatus.GODKJENT_MINUSBELØP, RefusjonStatus.GODKJENT_NULLBELØP)
            refusjonRepository.findAllByRefusjonsgrunnlag_Tilskuddsgrunnlag_AvtaleNrAndStatusIn(refusjon.refusjonsgrunnlag.tilskuddsgrunnlag.avtaleNr, statuser)
                .filter { YearMonth.from(it.refusjonsgrunnlag.tilskuddsgrunnlag.tilskuddFom) == YearMonth.from(refusjon.refusjonsgrunnlag.tilskuddsgrunnlag.tilskuddFom) }
                .forEach {
                    if (it.refusjonsgrunnlag.beregning?.fratrekkLønnFerie != 0 && !refusjon.refusjonsgrunnlag.harFerietrekkForSammeMåned) {
                        log.info("Ferietrekk er trukket på en tidligere refusjon: ${it.id} på samme avtalenr i samme måned på denne refusjonen: ${refusjon.id} setter harFerieTrekkForSammeMåned til true")
                        refusjon.refusjonsgrunnlag.harFerietrekkForSammeMåned = true
                    }
                }
            refusjonRepository.save(refusjon)
        }
    }

    fun oppdaterRefusjon(refusjon: Refusjon, utførtAv: InnloggetBruker) {
        log.info("Oppdaterer refusjon ${refusjon.id} med data")
        // Ikke oppdater tallgrunnlag på innsendte refusjoner
        if (refusjon.status.isUbehandlet()) {
            settMinusbeløpFraTidligereRefusjonerTilknyttetAvtalen(refusjon)
            if (refusjon.tiltakstype().har5gBegrensning()) {
                refusjon.refusjonsgrunnlag.sumUtbetaltVarig = totaltUtbetaltForTiltakMed5gBegrensning(refusjon)
            }
            settOmFerieErTrukketForSammeMåned(refusjon)
            oppdaterSistEndret(refusjon)
            gjørBeregning(refusjon, utførtAv)
        }
    }

    fun oppdaterSistEndret(refusjon: Refusjon) {
        refusjon.sistEndret = Instant.now()
    }

    fun gjørBeregning(refusjon: Refusjon, utførtAv: InnloggetBruker) {
        val beregning: Beregning? = beregnRefusjon(Beregningskontekst(grunnbelopService.alleGrunnbelop()), refusjon)
        if (beregning != null) {
            refusjon.refusjonsgrunnlag.beregning = beregning
            log.info("Oppdatert beregning på refusjon ${refusjon.id} til ${beregning.id}")
            applicationEventPublisher.publishEvent(BeregningUtført(refusjon, utførtAv))
        }
    }

    fun gjørKorreksjonBeregning(korreksjon: Korreksjon, utførtAv: InnloggetBruker) {
        val beregning = beregnKorreksjon(Beregningskontekst(grunnbelopService.alleGrunnbelop()), korreksjon)
        if (beregning != null) {
            korreksjon.refusjonsgrunnlag.beregning = beregning
            applicationEventPublisher.publishEvent(KorreksjonBeregningUtført(korreksjon, utførtAv))
        }
    }

    fun endreBruttolønn(refusjon: Refusjon, inntekterKunFraTiltaket: Boolean?, bruttoLønn: Int?) {
        refusjon.endreBruttolønn(inntekterKunFraTiltaket, bruttoLønn)
    }

    @Transactional
    fun utførAutomatiskInnsendingHvisMulig(refusjon: Refusjon) {
        if (!refusjon.settKlarTilInnsendingHvisMulig()) {
            return
        }
        if (!refusjon.tiltakstype().utbetalesAutomatisk()) {
            throw IllegalStateException("Refusjon ${refusjon.id} kan ikke sendes inn automatisk (tiltakstype ${refusjon.tiltakstype()})")
        }
        log.info(
            "Utfører automatisk innsending av refusjon {}-{} ({})",
            refusjon.refusjonsgrunnlag.tilskuddsgrunnlag.avtaleNr,
            refusjon.refusjonsgrunnlag.tilskuddsgrunnlag.løpenummer,
            refusjon.id
        )
        this.gjørBeregning(refusjon, SYSTEM_BRUKER)
        this.gjørBedriftKontonummeroppslag(refusjon)
        refusjon.godkjennForArbeidsgiver(utførtAv = SYSTEM_BRUKER)
        refusjonRepository.save(refusjon)
    }
}

private fun Refundering.fraSammeÅrSom(refusjon: Refundering): Boolean =
    this.refusjonsgrunnlag.tilskuddsgrunnlag.tilskuddFom.year.equals(refusjon.refusjonsgrunnlag.tilskuddsgrunnlag.tilskuddFom.year)
