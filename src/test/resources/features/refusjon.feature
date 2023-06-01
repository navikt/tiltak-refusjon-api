#language: no
Egenskap: Beregn refusjon for sommerjobb

  Scenario: Inntekt uten opptjeningsperiode
    Gitt følgende opplysninger om inntekt
      | inntektType   | beskrivelse | beløp | måned   | opptjeningsperiodeFom | opptjeningsperiodeTom | erOpptjentIPeriode |
      | LOENNSINNTEKT | fastloenn   | 20000 | 2020-01 |                       |                       | true           |
    Når sommerjobb på 60 prosent skal refunderes for periode "2020-01-01" til "2020-01-31" med arbeidsgiveravgift "0.141", feriepengersats "0.12", OTP-sats "0.02"
    Så beregnes refusjon til 15642 kr for periode

  Scenario: Inntekt etter tilskuddsperiode, ved korreksjon
    Gitt følgende opplysninger om inntekt
      | inntektType   | beskrivelse | beløp | måned   | opptjeningsperiodeFom | opptjeningsperiodeTom | erOpptjentIPeriode |
      | LOENNSINNTEKT | fastloenn   | 20000 | 2020-02 |                       |                       | true           |
    Når sommerjobb på 60 prosent skal refunderes for periode "2020-01-01" til "2020-01-31" med arbeidsgiveravgift "0.141", feriepengersats "0.12", OTP-sats "0.02"
    Og korreksjonsgrunn "HENT_INNTEKTER_TO_MÅNEDER_FREM" er valgt
    Så beregnes refusjon til 15642 kr for periode

  @skip_scenario
  Scenario: Inntekt etter tilskuddsperiode, ikke korreksjon
    Gitt følgende opplysninger om inntekt
      | inntektType   | beskrivelse | beløp | måned   | opptjeningsperiodeFom | opptjeningsperiodeTom | erOpptjentIPeriode |
      | LOENNSINNTEKT | fastloenn   | 20000 | 2020-02 |                       |                       | true           |
    Når sommerjobb på 60 prosent skal refunderes for periode "2020-01-01" til "2020-01-31" med arbeidsgiveravgift "0.141", feriepengersats "0.12", OTP-sats "0.02"
    Så beregnes refusjon til 0 kr for periode

  Scenario: Inntekt med og uten opptjeningsperiode
    Gitt følgende opplysninger om inntekt
      | inntektType   | beskrivelse | beløp | måned   | opptjeningsperiodeFom | opptjeningsperiodeTom | erOpptjentIPeriode |
      | LOENNSINNTEKT | fastloenn   | 10000 | 2020-03 | 2020-03-20            | 2020-04-10            | true           |
      | LOENNSINNTEKT | fastloenn   | 5000  | 2020-03 |                       |                       | true           |
      | LOENNSINNTEKT | fastloenn   | 5000  | 2020-04 |                       |                       | true           |
    Når sommerjobb på 60 prosent skal refunderes for periode "2020-03-20" til "2020-04-10" med arbeidsgiveravgift "0.141", feriepengersats "0.12", OTP-sats "0.02"
    Så beregnes refusjon til 15642 kr for periode

  @skip_scenario
  Scenario: Inntekt uten opptjeningsperiode, utenfor tilskuddsperiode
    Gitt følgende opplysninger om inntekt
      | inntektType   | beskrivelse | beløp | måned   | opptjeningsperiodeFom | opptjeningsperiodeTom | erOpptjentIPeriode |
      | LOENNSINNTEKT | fastloenn   | 10000 | 2020-03 | 2020-03-20            | 2020-04-10            | true           |
      | LOENNSINNTEKT | fastloenn   | 5000  | 2020-02 |                       |                       | true           |
      | LOENNSINNTEKT | fastloenn   | 5000  | 2020-05 |                       |                       | true           |
    Når sommerjobb på 60 prosent skal refunderes for periode "2020-03-20" til "2020-04-10" med arbeidsgiveravgift "0.141", feriepengersats "0.12", OTP-sats "0.02"
    Så beregnes refusjon til 7821 kr for periode

  Scenario: To inntekter i hele tilskuddsperioden
    Gitt følgende opplysninger om inntekt
      | inntektType   | beskrivelse | beløp | måned   | opptjeningsperiodeFom | opptjeningsperiodeTom | erOpptjentIPeriode |
      | LOENNSINNTEKT | fastloenn   | 10000 | 2020-03 | 2020-03-20            | 2020-04-10            | true           |
      | LOENNSINNTEKT | fastloenn   | 20000 | 2020-03 | 2020-03-20            | 2020-04-10            | true           |
    Når sommerjobb på 60 prosent skal refunderes for periode "2020-03-20" til "2020-04-10" med arbeidsgiveravgift "0.141", feriepengersats "0.12", OTP-sats "0.02"
    Så beregnes refusjon til 23463 kr for periode

  Scenario: Ferie i mellom inntekter
    Gitt følgende opplysninger om inntekt
      | inntektType   | beskrivelse | beløp | måned   | opptjeningsperiodeFom | opptjeningsperiodeTom | erOpptjentIPeriode |
      | LOENNSINNTEKT | fastloenn   | 10000 | 2020-04 | 2020-04-01            | 2020-04-10            | true           |
      | FERIE         | fastloenn   | 5000  | 2020-04 | 2020-04-11            | 2020-04-17            | true           |
      | LOENNSINNTEKT | fastloenn   | 20000 | 2020-04 | 2020-04-18            | 2020-04-30            | true           |
    Når sommerjobb på 60 prosent skal refunderes for periode "2020-01-04" til "2020-04-30" med arbeidsgiveravgift "0.141", feriepengersats "0.12", OTP-sats "0.02"
    Så beregnes refusjon til 23463 kr for periode

  @skip_scenario
  Scenario: Inntekt før tilskuddsperiode, og ytelse
    Gitt følgende opplysninger om inntekt
      | inntektType   | beskrivelse | beløp | måned   | opptjeningsperiodeFom | opptjeningsperiodeTom | erOpptjentIPeriode |
      | LOENNSINNTEKT | fastloenn   | 7000  | 2019-01 | 2019-01-01            | 2019-01-31            | true           |
      | YTELSE        | fastloenn   | 2000  | 2020-01 |                       |                       | true           |
    Når sommerjobb på 60 prosent skal refunderes for periode "2020-01-01" til "2020-01-31" med arbeidsgiveravgift "0.141", feriepengersats "0.12", OTP-sats "0.02"
    Så beregnes refusjon til 0 kr for periode

  @skip_scenario
  Scenario: Inntekt etter tilskuddsperiode, og ytelse
    Gitt følgende opplysninger om inntekt
      | inntektType   | beskrivelse | beløp | måned   | opptjeningsperiodeFom | opptjeningsperiodeTom | erOpptjentIPeriode |
      | LOENNSINNTEKT | fastloenn   | 7000  | 2020-02 | 2020-02-01            | 2020-02-28            | true           |
      | YTELSE        | fastloenn   | 2000  | 2020-01 |                       |                       | true           |
    Når sommerjobb på 60 prosent skal refunderes for periode "2020-01-01" til "2020-01-31" med arbeidsgiveravgift "0.141", feriepengersats "0.12", OTP-sats "0.02"
    Så beregnes refusjon til 0 kr for periode

  Scenario: Tjener mer enn tilskuddsbeløp, skal da avkorte refusjon
    Gitt følgende opplysninger om inntekt
      | inntektType   | beskrivelse | beløp | måned   | opptjeningsperiodeFom | opptjeningsperiodeTom | erOpptjentIPeriode |
      | LOENNSINNTEKT | fastloenn   | 10000 | 2020-04 | 2020-04-01            | 2020-04-30            | true           |
    Når sommerjobb på 51 prosent skal refunderes for periode "2020-04-01" til "2020-04-30" med arbeidsgiveravgift "0.0", feriepengersats "0.0", OTP-sats "0.0"
    Og tilskuddsbeløp er 5000 kr
    Så beregnes refusjon til 5000 kr for periode

  Scenario: Er korreksjon, skal da avkorte refusjon
    Gitt følgende opplysninger om inntekt
      | inntektType   | beskrivelse | beløp | måned   | opptjeningsperiodeFom | opptjeningsperiodeTom | erOpptjentIPeriode |
      | LOENNSINNTEKT | fastloenn   | 10000 | 2020-04 | 2020-04-01            | 2020-04-30            | true           |
    Når sommerjobb på 50 prosent skal refunderes for periode "2020-04-01" til "2020-04-30" med arbeidsgiveravgift "0.0", feriepengersats "0.0", OTP-sats "0.0"
    Og tidligere utbetalt er 4999 kr
    Så beregnes refusjon til 1 kr for periode

  Scenario: Bruttolønnkorreksjon som er lavere enn innhentet bruttolønn
    Gitt følgende opplysninger om inntekt
      | inntektType   | beskrivelse | beløp | måned   | opptjeningsperiodeFom | opptjeningsperiodeTom | erOpptjentIPeriode |
      | LOENNSINNTEKT | fastloenn   | 10000 | 2020-04 | 2020-04-01            | 2020-04-30            | true           |
    Når sommerjobb på 50 prosent skal refunderes for periode "2020-04-01" til "2020-04-30" med arbeidsgiveravgift "0.0", feriepengersats "0.0", OTP-sats "0.0"
    Og bruttolønn er korrigert til 5000 kr
    Så beregnes refusjon til 2500 kr for periode

  Scenario: Bruttolønnkorreksjon som er høyere enn innhentet bruttolønn
    Gitt følgende opplysninger om inntekt
      | inntektType   | beskrivelse | beløp | måned   | opptjeningsperiodeFom | opptjeningsperiodeTom | erOpptjentIPeriode |
      | LOENNSINNTEKT | fastloenn   | 10000 | 2020-04 | 2020-04-01            | 2020-04-30            | true           |
    Når sommerjobb på 50 prosent skal refunderes for periode "2020-04-01" til "2020-04-30" med arbeidsgiveravgift "0.0", feriepengersats "0.0", OTP-sats "0.0"
    Og bruttolønn er korrigert til 11000 kr
    Så beregnes refusjon til 5000 kr for periode

  Scenario: Skal filtrere ut riktige lønnsinntekter
    Gitt følgende opplysninger om inntekt
      | inntektType   | beskrivelse                                                                       | beløp | måned   | opptjeningsperiodeFom | opptjeningsperiodeTom | erOpptjentIPeriode |
      | LOENNSINNTEKT | fastloenn                                                                         | 10000 | 2020-04 | 2020-04-01            | 2020-04-30            | true           |
      | LOENNSINNTEKT | timeloenn                                                                         | 10000 | 2020-04 | 2020-04-01            | 2020-04-30            | true           |
      | LOENNSINNTEKT | Honorar/Akkord/Prosent/Provisjon                                                  | 10000 | 2020-04 | 2020-04-01            | 2020-04-30            | true           |
      | LOENNSINNTEKT | fastTillegg                                                                       | 10000 | 2020-04 | 2020-04-01            | 2020-04-30            | true           |
      | LOENNSINNTEKT | uregelmessigeTilleggKnyttetTilArbeidetTid                                         | 10000 | 2020-04 | 2020-04-01            | 2020-04-30            | true           |
      | LOENNSINNTEKT | Helligdagstillegg                                                                 | 10000 | 2020-04 | 2020-04-01            | 2020-04-30            | true           |
      | LOENNSINNTEKT | Uregelmessige tillegg knyttet til ikke- arbeidet tid                              | 10000 | 2020-04 | 2020-04-01            | 2020-04-30            | true           |
      | LOENNSINNTEKT | Bonus                                                                             | 10000 | 2020-04 | 2020-04-01            | 2020-04-30            | true           |
      | LOENNSINNTEKT | Overtidsgodtgjørelse                                                              | 10000 | 2020-04 | 2020-04-01            | 2020-04-30            | true           |
      | LOENNSINNTEKT | Styrehonorar og godtgjørelse i forbindelse med verv                               | 10000 | 2020-04 | 2020-04-01            | 2020-04-30            | true           |
      | LOENNSINNTEKT | Sluttvederlag                                                                     | 10000 | 2020-04 | 2020-04-01            | 2020-04-30            | true           |
      | LOENNSINNTEKT | Feriepenger                                                                       | 10000 | 2020-04 | 2020-04-01            | 2020-04-30            | true           |
      | LOENNSINNTEKT | Trekk i lønn for ferie                                                            | 10000 | 2020-04 | 2020-04-01            | 2020-04-30            | true           |
      | LOENNSINNTEKT | Annet                                                                             | 10000 | 2020-04 | 2020-04-01            | 2020-04-30            | true           |
      | LOENNSINNTEKT | Beregnet Skatt                                                                    | 10000 | 2020-04 | 2020-04-01            | 2020-04-30            | true           |
      | LOENNSINNTEKT | Bonus fra forsvaret                                                               | 10000 | 2020-04 | 2020-04-01            | 2020-04-30            | true           |
      | LOENNSINNTEKT | Fond for idrettsutøvere                                                           | 10000 | 2020-04 | 2020-04-01            | 2020-04-30            | true           |
      | LOENNSINNTEKT | Hyretillegg                                                                       | 10000 | 2020-04 | 2020-04-01            | 2020-04-30            | true           |
      | LOENNSINNTEKT | Hyretillegg (utgår 01-2018)                                                       | 10000 | 2020-04 | 2020-04-01            | 2020-04-30            | true           |
      | LOENNSINNTEKT | Lønn og godtgjørelse til dagmamma eller praktikant som passer barn i barnets hjem | 10000 | 2020-04 | 2020-04-01            | 2020-04-30            | true           |
      | LOENNSINNTEKT | Lønn og godtgjørelse til privatpersoner for arbeidsoppdrag i oppdragsgivers hjem  | 10000 | 2020-04 | 2020-04-01            | 2020-04-30            | true           |
      | LOENNSINNTEKT | Lønn utbetalt av veldedig eller allmennyttig institusjon eller organisasjon       | 10000 | 2020-04 | 2020-04-01            | 2020-04-30            | true           |
      | LOENNSINNTEKT | Lønn utenlandsk artist                                                            | 10000 | 2020-04 | 2020-04-01            | 2020-04-30            | true           |
      | LOENNSINNTEKT | Lønn                                                                              | 10000 | 2020-04 | 2020-04-01            | 2020-04-30            | true           |
      | LOENNSINNTEKT | Lønn til verge fra Fylkesmannen                                                   | 10000 | 2020-04 | 2020-04-01            | 2020-04-30            | true           |
      | LOENNSINNTEKT | Opsjoner                                                                          | 10000 | 2020-04 | 2020-04-01            | 2020-04-30            | true           |
      | LOENNSINNTEKT | Betalt utenlandsk skatt                                                           | 10000 | 2020-04 | 2020-04-01            | 2020-04-30            | true           |
      | LOENNSINNTEKT | Tips                                                                              | 10000 | 2020-04 | 2020-04-01            | 2020-04-30            | true           |
      | LOENNSINNTEKT | Aksjer/grunnfondsbevis til underkurs                                              | 10000 | 2020-04 | 2020-04-01            | 2020-04-30            | true           |
    Når sommerjobb på 50 prosent skal refunderes for periode "2020-04-01" til "2020-04-30" med arbeidsgiveravgift "0.0", feriepengersats "0.0", OTP-sats "0.0"
    Så beregnes refusjon til 20000 kr for periode