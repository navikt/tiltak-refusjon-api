#language: no
Egenskap: Beregn refusjon for lønnstilskudd

  Scenario: Inntekt uten opptjeningsperiode
    Gitt følgende opplysninger om inntekt
      | inntektType   | beløp | måned   | opptjeningsperiodeFom | opptjeningsperiodeTom |
      | LOENNSINNTEKT | 20000 | 2020-01 |                       |                       |
    Når lønnstilskudd på 60 prosent skal refunderes for periode "2020-01-01" til "2020-01-31" med arbeidsgiveravgift "0.141", feriepengersats "0.12", OTP-sats "0.02"
    Så beregnes refusjon til 15642 kr for periode

  Scenario: Inntekt med og uten opptjeningsperiode
    Gitt følgende opplysninger om inntekt
      | inntektType   | beløp | måned   | opptjeningsperiodeFom | opptjeningsperiodeTom |
      | LOENNSINNTEKT | 10000 | 2020-03 | 2020-03-20            | 2020-04-10            |
      | LOENNSINNTEKT | 10000 | 2020-04 |                       |                       |
    Når lønnstilskudd på 40 prosent skal refunderes for periode "2020-03-20" til "2020-04-10" med arbeidsgiveravgift "0.141", feriepengersats "0.12", OTP-sats "0.02"
    Så beregnes refusjon til 6952 kr for periode

  Scenario: To inntekter i hele tilskuddsperioden
    Gitt følgende opplysninger om inntekt
      | inntektType   | beløp | måned   | opptjeningsperiodeFom | opptjeningsperiodeTom |
      | LOENNSINNTEKT | 10000 | 2020-03 | 2020-03-20            | 2020-04-10            |
      | LOENNSINNTEKT | 20000 | 2020-03 | 2020-03-20            | 2020-04-10            |
    Når lønnstilskudd på 60 prosent skal refunderes for periode "2020-03-20" til "2020-04-10" med arbeidsgiveravgift "0.141", feriepengersats "0.12", OTP-sats "0.02"
    Så beregnes refusjon til 23463 kr for periode

  Scenario: Ferie i mellom inntekter
    Gitt følgende opplysninger om inntekt
      | inntektType   | beløp | måned   | opptjeningsperiodeFom | opptjeningsperiodeTom |
      | LOENNSINNTEKT | 10000 | 2020-04 | 2020-04-01            | 2020-04-10            |
      | FERIE         | 5000  | 2020-04 | 2020-04-11            | 2020-04-17            |
      | LOENNSINNTEKT | 20000 | 2020-04 | 2020-04-18            | 2020-04-30            |
    Når lønnstilskudd på 60 prosent skal refunderes for periode "2020-01-04" til "2020-04-30" med arbeidsgiveravgift "0.141", feriepengersats "0.12", OTP-sats "0.02"
    Så beregnes refusjon til 23463 kr for periode

  Scenario: Inntekt utenfor tilskuddsperiode, og ytelse
    Gitt følgende opplysninger om inntekt
      | inntektType   | beløp | måned   | opptjeningsperiodeFom | opptjeningsperiodeTom |
      | LOENNSINNTEKT | 7000  | 2019-01 | 2019-01-01            | 2019-01-31            |
      | YTELSE        | 2000  | 2020-01 |                       |                       |
    Når lønnstilskudd på 60 prosent skal refunderes for periode "2020-01-01" til "2020-01-31" med arbeidsgiveravgift "0.141", feriepengersats "0.12", OTP-sats "0.02"
    Så beregnes refusjon til 0 kr for periode

  Scenario: Jobber bare helg
    Gitt følgende opplysninger om inntekt
      | inntektType   | beløp | måned   | opptjeningsperiodeFom | opptjeningsperiodeTom |
      | LOENNSINNTEKT | 10000 | 2020-03 | 2020-04-04            | 2020-04-05            |
    Når lønnstilskudd på 40 prosent skal refunderes for periode "2020-04-01" til "2020-04-30" med arbeidsgiveravgift "0.141", feriepengersats "0.12", OTP-sats "0.02"
    Så beregnes refusjon til 5214 kr for periode

  Scenario: Tjener mer enn tilskuddsbeløp, skal da avkorte refusjon
    Gitt følgende opplysninger om inntekt
      | inntektType   | beløp | måned   | opptjeningsperiodeFom | opptjeningsperiodeTom |
      | LOENNSINNTEKT | 10000 | 2020-03 | 2020-04-01            | 2020-04-30            |
    Når lønnstilskudd på 51 prosent skal refunderes for periode "2020-04-01" til "2020-04-30" med arbeidsgiveravgift "0.0", feriepengersats "0.0", OTP-sats "0.0"
    Og tilskuddsbeløp er 5000 kr
    Så beregnes refusjon til 5000 kr for periode