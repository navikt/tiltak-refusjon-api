package no.nav.arbeidsgiver.tiltakrefusjon.refusjon


import no.nav.arbeidsgiver.tiltakrefusjon.inntekt.InntektskomponentService
import no.nav.arbeidsgiver.tiltakrefusjon.okonomi.KontoregisterService
import no.nav.arbeidsgiver.tiltakrefusjon.tilskuddsperiode.MidlerFrigjortÅrsak
import no.nav.arbeidsgiver.tiltakrefusjon.tilskuddsperiode.TilskuddsperiodeAnnullertMelding
import no.nav.arbeidsgiver.tiltakrefusjon.tilskuddsperiode.TilskuddsperiodeForkortetMelding
import no.nav.arbeidsgiver.tiltakrefusjon.tilskuddsperiode.TilskuddsperiodeGodkjentMelding
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class RefusjonService(
    val inntektskomponentService: InntektskomponentService,
    val refusjonRepository: RefusjonRepository,
    val korreksjonRepository: KorreksjonRepository,
    val kontoregisterService: KontoregisterService,
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
            resendingsNummer = tilskuddsperiodeGodkjentMelding.resendingsNummer,
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
    fun settMinusBeløpOmFratrukketFerieGirMinusForForrigeRefusjonOmDenFinnes(denneRefusjon: Refusjon) {
        val tidligereRefusjonMedMinusBeløpEtterFratrukketFerie: Refusjon =
            refusjonRepository.finnRefusjonSomSkalSendesMedMinusBeløpEtterFratrukketFerieFørDenne(
                denneRefusjon.bedriftNr,
                denneRefusjon.tilskuddsgrunnlag.avtaleNr,
                denneRefusjon.tilskuddsgrunnlag.tiltakstype,
                RefusjonStatus.GODKJENT_MINUSBELØP,
                denneRefusjon.tilskuddsgrunnlag.løpenummer
            ) ?: return

        if (tidligereRefusjonMedMinusBeløpEtterFratrukketFerie.beregning!!.lønnFratrukketFerie <= 0)
            denneRefusjon.refusjonsgrunnlag.oppgiForrigeRefusjonsbeløp(tidligereRefusjonMedMinusBeløpEtterFratrukketFerie.beregning!!.refusjonsbeløp)
    }

    fun settOmForrigeRefusjonMåSendesFørst(refusjon: Refusjon){
        if(refusjon.status != RefusjonStatus.KLAR_FOR_INNSENDING) return
        val forrigeRefusjonSomMåSendesInnFørst: Refusjon = refusjonRepository.finnRefusjonSomSkalSendesFørDenne(refusjon.bedriftNr,refusjon.tilskuddsgrunnlag.avtaleNr,refusjon.tilskuddsgrunnlag.tiltakstype, RefusjonStatus.KLAR_FOR_INNSENDING, refusjon.tilskuddsgrunnlag.løpenummer).firstOrNull()
                ?: return
        if(forrigeRefusjonSomMåSendesInnFørst != refusjon) refusjon.angiRefusjonSomMåSendesFørst(forrigeRefusjonSomMåSendesInnFørst)
    }

    fun settMinusBeløpOmFratrukketFerieGirMinusForForrigeRefusjonOmDenFinnes(denneKorreksjon: Korreksjon) {
        val tidligereRefusjonMedMinusBeløpEtterFratrukketFerie: Refusjon =
            refusjonRepository.finnRefusjonSomSkalSendesMedMinusBeløpEtterFratrukketFerieFørDenne(
                denneKorreksjon.bedriftNr,
                denneKorreksjon.refusjonsgrunnlag.tilskuddsgrunnlag.avtaleNr,
                denneKorreksjon.refusjonsgrunnlag.tilskuddsgrunnlag.tiltakstype,
                RefusjonStatus.GODKJENT_MINUSBELØP,
                denneKorreksjon.refusjonsgrunnlag.tilskuddsgrunnlag.løpenummer
            ) ?: return

        if (tidligereRefusjonMedMinusBeløpEtterFratrukketFerie.beregning!!.lønnFratrukketFerie <= 0)
            denneKorreksjon.refusjonsgrunnlag.oppgiForrigeRefusjonsbeløp(tidligereRefusjonMedMinusBeløpEtterFratrukketFerie.beregning!!.refusjonsbeløp)
    }

    fun gjørInntektsoppslag(refusjon: Refusjon) {
        if (!refusjon.skalGjøreInntektsoppslag()) {
            return
        }

        var antallEkstraMånederSomSkalSjekkes: Long = 0
        if (refusjon.unntakOmInntekterToMånederFrem) {
            antallEkstraMånederSomSkalSjekkes = 2
        } else if (refusjon.tilskuddsgrunnlag.tiltakstype === Tiltakstype.SOMMERJOBB) {
            antallEkstraMånederSomSkalSjekkes = 1
        }
        try {
            val inntektsoppslag = inntektskomponentService.hentInntekter(
                fnr = refusjon.deltakerFnr,
                bedriftnummerDetSøkesPå = refusjon.bedriftNr,
                datoFra = refusjon.refusjonsgrunnlag.tilskuddsgrunnlag.tilskuddFom,
                datoTil = refusjon.refusjonsgrunnlag.tilskuddsgrunnlag.tilskuddTom.plusMonths(antallEkstraMånederSomSkalSjekkes)
            )
            val inntektsgrunnlag = Inntektsgrunnlag(
                inntekter = inntektsoppslag.first,
                respons = inntektsoppslag.second
            )
            refusjon.oppgiInntektsgrunnlag(inntektsgrunnlag, refusjon.inntektsgrunnlag)
            refusjonRepository.save(refusjon)
        } catch (e: Exception) {
            log.error("Feil ved henting av inntekter for refusjon ${refusjon.id}", e)
        }
    }

    fun godkjennForArbeidsgiver(refusjon: Refusjon, utførtAv: String) {
        if(måGodkjenneTidligereRefusjonerFørst(refusjon)){
            throw GodkjennEldreRefusjonFørstException()
        }
        refusjon.godkjennForArbeidsgiver(utførtAv)
        refusjonRepository.save(refusjon)
    }

    private fun måGodkjenneTidligereRefusjonerFørst(refusjon:Refusjon): Boolean{
        if(refusjon.status != RefusjonStatus.KLAR_FOR_INNSENDING) return false
        val forrigeRefusjonSomMåSendesInnFørst: Refusjon? = refusjonRepository.finnRefusjonSomSkalSendesFørDenne(refusjon.bedriftNr,refusjon.tilskuddsgrunnlag.avtaleNr,refusjon.tilskuddsgrunnlag.tiltakstype, RefusjonStatus.KLAR_FOR_INNSENDING, refusjon.tilskuddsgrunnlag.løpenummer).firstOrNull()
        return  forrigeRefusjonSomMåSendesInnFørst != null && forrigeRefusjonSomMåSendesInnFørst != refusjon
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

    fun opprettKorreksjonsutkast(refusjon: Refusjon, korreksjonsgrunner: Set<Korreksjonsgrunn>): Korreksjon {
        val korreksjonsutkast = refusjon.opprettKorreksjonsutkast(korreksjonsgrunner)
        korreksjonRepository.save(korreksjonsutkast)
        refusjonRepository.save(refusjon)
        return korreksjonsutkast
    }
}