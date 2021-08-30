#language: no
Egenskap: Beregn refusjon for sommerjobb

  Scenario: Inntekt uten opptjeningsperiode
    Gitt følgende opplysninger om inntekt
      | inntektType   | beskrivelse | beløp | måned   | opptjeningsperiodeFom | opptjeningsperiodeTom |
      | LOENNSINNTEKT | fastloenn   | 20000 | 2020-01 |                       |                       |
    Når sommerjobb på 60 prosent skal refunderes for periode "2020-01-01" til "2020-01-31" med arbeidsgiveravgift "0.141", feriepengersats "0.12", OTP-sats "0.02"
    Så beregnes refusjon til 15642 kr for periode

  Scenario: Inntekt etter tilskuddsperiode, ved korreksjon
    Gitt følgende opplysninger om inntekt
      | inntektType   | beskrivelse | beløp | måned   | opptjeningsperiodeFom | opptjeningsperiodeTom |
      | LOENNSINNTEKT | fastloenn   | 20000 | 2020-02 |                       |                       |
    Når sommerjobb på 60 prosent skal refunderes for periode "2020-01-01" til "2020-01-31" med arbeidsgiveravgift "0.141", feriepengersats "0.12", OTP-sats "0.02"
    Og korreksjonsgrunn "INNTEKTER_RAPPORTERT_ETTER_TILSKUDDSPERIODE" er valgt
    Så beregnes refusjon til 15642 kr for periode

  Scenario: Inntekt etter tilskuddsperiode, ikke korreksjon
    Gitt følgende opplysninger om inntekt
      | inntektType   | beskrivelse | beløp | måned   | opptjeningsperiodeFom | opptjeningsperiodeTom |
      | LOENNSINNTEKT | fastloenn   | 20000 | 2020-02 |                       |                       |
    Når sommerjobb på 60 prosent skal refunderes for periode "2020-01-01" til "2020-01-31" med arbeidsgiveravgift "0.141", feriepengersats "0.12", OTP-sats "0.02"
    Så beregnes refusjon til 0 kr for periode

  Scenario: Inntekt med og uten opptjeningsperiode
    Gitt følgende opplysninger om inntekt
      | inntektType   | beskrivelse | beløp | måned   | opptjeningsperiodeFom | opptjeningsperiodeTom |
      | LOENNSINNTEKT | fastloenn   | 10000 | 2020-03 | 2020-03-20            | 2020-04-10            |
      | LOENNSINNTEKT | fastloenn   | 5000  | 2020-03 |                       |                       |
      | LOENNSINNTEKT | fastloenn   | 5000  | 2020-04 |                       |                       |
    Når sommerjobb på 60 prosent skal refunderes for periode "2020-03-20" til "2020-04-10" med arbeidsgiveravgift "0.141", feriepengersats "0.12", OTP-sats "0.02"
    Så beregnes refusjon til 15642 kr for periode

  Scenario: Inntekt uten opptjeningsperiode, utenfor tilskuddsperiode
    Gitt følgende opplysninger om inntekt
      | inntektType   | beskrivelse | beløp | måned   | opptjeningsperiodeFom | opptjeningsperiodeTom |
      | LOENNSINNTEKT | fastloenn   | 10000 | 2020-03 | 2020-03-20            | 2020-04-10            |
      | LOENNSINNTEKT | fastloenn   | 5000  | 2020-02 |                       |                       |
      | LOENNSINNTEKT | fastloenn   | 5000  | 2020-05 |                       |                       |
    Når sommerjobb på 60 prosent skal refunderes for periode "2020-03-20" til "2020-04-10" med arbeidsgiveravgift "0.141", feriepengersats "0.12", OTP-sats "0.02"
    Så beregnes refusjon til 7821 kr for periode

  Scenario: To inntekter i hele tilskuddsperioden
    Gitt følgende opplysninger om inntekt
      | inntektType   | beskrivelse | beløp | måned   | opptjeningsperiodeFom | opptjeningsperiodeTom |
      | LOENNSINNTEKT | fastloenn   | 10000 | 2020-03 | 2020-03-20            | 2020-04-10            |
      | LOENNSINNTEKT | fastloenn   | 20000 | 2020-03 | 2020-03-20            | 2020-04-10            |
    Når sommerjobb på 60 prosent skal refunderes for periode "2020-03-20" til "2020-04-10" med arbeidsgiveravgift "0.141", feriepengersats "0.12", OTP-sats "0.02"
    Så beregnes refusjon til 23463 kr for periode

  Scenario: Ferie i mellom inntekter
    Gitt følgende opplysninger om inntekt
      | inntektType   | beskrivelse | beløp | måned   | opptjeningsperiodeFom | opptjeningsperiodeTom |
      | LOENNSINNTEKT | fastloenn   | 10000 | 2020-04 | 2020-04-01            | 2020-04-10            |
      | FERIE         | fastloenn   | 5000  | 2020-04 | 2020-04-11            | 2020-04-17            |
      | LOENNSINNTEKT | fastloenn   | 20000 | 2020-04 | 2020-04-18            | 2020-04-30            |
    Når sommerjobb på 60 prosent skal refunderes for periode "2020-01-04" til "2020-04-30" med arbeidsgiveravgift "0.141", feriepengersats "0.12", OTP-sats "0.02"
    Så beregnes refusjon til 23463 kr for periode

  Scenario: Inntekt før tilskuddsperiode, og ytelse
    Gitt følgende opplysninger om inntekt
      | inntektType   | beskrivelse | beløp | måned   | opptjeningsperiodeFom | opptjeningsperiodeTom |
      | LOENNSINNTEKT | fastloenn   | 7000  | 2019-01 | 2019-01-01            | 2019-01-31            |
      | YTELSE        | fastloenn   | 2000  | 2020-01 |                       |                       |
    Når sommerjobb på 60 prosent skal refunderes for periode "2020-01-01" til "2020-01-31" med arbeidsgiveravgift "0.141", feriepengersats "0.12", OTP-sats "0.02"
    Så beregnes refusjon til 0 kr for periode

  Scenario: Inntekt etter tilskuddsperiode, og ytelse
    Gitt følgende opplysninger om inntekt
      | inntektType   | beskrivelse | beløp | måned   | opptjeningsperiodeFom | opptjeningsperiodeTom |
      | LOENNSINNTEKT | fastloenn   | 7000  | 2020-02 | 2020-02-01            | 2020-02-28            |
      | YTELSE        | fastloenn   | 2000  | 2020-01 |                       |                       |
    Når sommerjobb på 60 prosent skal refunderes for periode "2020-01-01" til "2020-01-31" med arbeidsgiveravgift "0.141", feriepengersats "0.12", OTP-sats "0.02"
    Så beregnes refusjon til 0 kr for periode

  Scenario: Tjener mer enn tilskuddsbeløp, skal da avkorte refusjon
    Gitt følgende opplysninger om inntekt
      | inntektType   | beskrivelse | beløp | måned   | opptjeningsperiodeFom | opptjeningsperiodeTom |
      | LOENNSINNTEKT | fastloenn   | 10000 | 2020-04 | 2020-04-01            | 2020-04-30            |
    Når sommerjobb på 51 prosent skal refunderes for periode "2020-04-01" til "2020-04-30" med arbeidsgiveravgift "0.0", feriepengersats "0.0", OTP-sats "0.0"
    Og tilskuddsbeløp er 5000 kr
    Så beregnes refusjon til 5000 kr for periode

  Scenario: Er korreksjon, skal da avkorte refusjon
    Gitt følgende opplysninger om inntekt
      | inntektType   | beskrivelse | beløp | måned   | opptjeningsperiodeFom | opptjeningsperiodeTom |
      | LOENNSINNTEKT | fastloenn   | 10000 | 2020-04 | 2020-04-01            | 2020-04-30            |
    Når sommerjobb på 51 prosent skal refunderes for periode "2020-04-01" til "2020-04-30" med arbeidsgiveravgift "0.0", feriepengersats "0.0", OTP-sats "0.0"
    Og tilskuddsbeløp er 5000 kr
    Og tidligere utbetalt er 4999 kr
    Så beregnes refusjon til 1 kr for periode

  Scenario: Skal filtrere ut riktige lønnsinntekter
    Gitt følgende opplysninger om inntekt
      | inntektType   | beskrivelse                                                                       | beløp | måned   | opptjeningsperiodeFom | opptjeningsperiodeTom |
      | LOENNSINNTEKT | fastloenn                                                                         | 10000 | 2020-04 | 2020-04-01            | 2020-04-30            |
      | LOENNSINNTEKT | timeloenn                                                                         | 10000 | 2020-04 | 2020-04-01            | 2020-04-30            |
      | LOENNSINNTEKT | Honorar/Akkord/Prosent/Provisjon                                                  | 10000 | 2020-04 | 2020-04-01            | 2020-04-30            |
      | LOENNSINNTEKT | fastTillegg                                                                       | 10000 | 2020-04 | 2020-04-01            | 2020-04-30            |
      | LOENNSINNTEKT | Uregelmessige tillegg knyttet til arbeidet tid                                    | 10000 | 2020-04 | 2020-04-01            | 2020-04-30            |
      | LOENNSINNTEKT | Helligdagstillegg                                                                 | 10000 | 2020-04 | 2020-04-01            | 2020-04-30            |
      | LOENNSINNTEKT | Uregelmessige tillegg knyttet til ikke- arbeidet tid                              | 10000 | 2020-04 | 2020-04-01            | 2020-04-30            |
      | LOENNSINNTEKT | Bonus                                                                             | 10000 | 2020-04 | 2020-04-01            | 2020-04-30            |
      | LOENNSINNTEKT | Overtidsgodtgjørelse                                                              | 10000 | 2020-04 | 2020-04-01            | 2020-04-30            |
      | LOENNSINNTEKT | Styrehonorar og godtgjørelse i forbindelse med verv                               | 10000 | 2020-04 | 2020-04-01            | 2020-04-30            |
      | LOENNSINNTEKT | Sluttvederlag                                                                     | 10000 | 2020-04 | 2020-04-01            | 2020-04-30            |
      | LOENNSINNTEKT | Feriepenger                                                                       | 10000 | 2020-04 | 2020-04-01            | 2020-04-30            |
      | LOENNSINNTEKT | Trekk i lønn for ferie                                                            | 10000 | 2020-04 | 2020-04-01            | 2020-04-30            |
      | LOENNSINNTEKT | Annet                                                                             | 10000 | 2020-04 | 2020-04-01            | 2020-04-30            |
      | LOENNSINNTEKT | Beregnet Skatt                                                                    | 10000 | 2020-04 | 2020-04-01            | 2020-04-30            |
      | LOENNSINNTEKT | Bonus fra forsvaret                                                               | 10000 | 2020-04 | 2020-04-01            | 2020-04-30            |
      | LOENNSINNTEKT | Fond for idrettsutøvere                                                           | 10000 | 2020-04 | 2020-04-01            | 2020-04-30            |
      | LOENNSINNTEKT | Hyretillegg                                                                       | 10000 | 2020-04 | 2020-04-01            | 2020-04-30            |
      | LOENNSINNTEKT | Hyretillegg (utgår 01-2018)                                                       | 10000 | 2020-04 | 2020-04-01            | 2020-04-30            |
      | LOENNSINNTEKT | Lønn og godtgjørelse til dagmamma eller praktikant som passer barn i barnets hjem | 10000 | 2020-04 | 2020-04-01            | 2020-04-30            |
      | LOENNSINNTEKT | Lønn og godtgjørelse til privatpersoner for arbeidsoppdrag i oppdragsgivers hjem  | 10000 | 2020-04 | 2020-04-01            | 2020-04-30            |
      | LOENNSINNTEKT | Lønn utbetalt av veldedig eller allmennyttig institusjon eller organisasjon       | 10000 | 2020-04 | 2020-04-01            | 2020-04-30            |
      | LOENNSINNTEKT | Lønn utenlandsk artist                                                            | 10000 | 2020-04 | 2020-04-01            | 2020-04-30            |
      | LOENNSINNTEKT | Lønn                                                                              | 10000 | 2020-04 | 2020-04-01            | 2020-04-30            |
      | LOENNSINNTEKT | Lønn til verge fra Fylkesmannen                                                   | 10000 | 2020-04 | 2020-04-01            | 2020-04-30            |
      | LOENNSINNTEKT | Opsjoner                                                                          | 10000 | 2020-04 | 2020-04-01            | 2020-04-30            |
      | LOENNSINNTEKT | Betalt utenlandsk skatt                                                           | 10000 | 2020-04 | 2020-04-01            | 2020-04-30            |
      | LOENNSINNTEKT | Tips                                                                              | 10000 | 2020-04 | 2020-04-01            | 2020-04-30            |
      | LOENNSINNTEKT | Aksjer/grunnfondsbevis til underkurs                                              | 10000 | 2020-04 | 2020-04-01            | 2020-04-30            |
    Når sommerjobb på 50 prosent skal refunderes for periode "2020-04-01" til "2020-04-30" med arbeidsgiveravgift "0.0", feriepengersats "0.0", OTP-sats "0.0"
    Så beregnes refusjon til 15000 kr for periode