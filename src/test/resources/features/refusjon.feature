#language: no
Egenskap: Beregn refusjon for lønnstilskudd

  Scenario: Beregn lønnstilskudd for deler av perioden med inntektsperiode dagpengersats samt OTP, Feriepenger og AV.avgift
    Gitt følgende opplysninger om inntekt
      | inntektType  | beløp | måned   | inntektsperiodeFom | inntektsperiodeTom |
      | LOENNSINNTEKT | 20000 | 2020-01 |                    |                    |
    Og avtale med arbeidsgiveravgift "0.141", feriepengersats "0.12"
    Når lønnstilskudd på 60 prosent skal refunderes for periode "2020-01-01" til "2020-01-31"
    Så beregnes refusjon til "15642" kr for periode

  Scenario: Beregn lønnstilskudd for deler av perioden med inntektsperiode dagpengersats
    Gitt følgende opplysninger om inntekt
      | inntektType  | beløp | måned   | inntektsperiodeFom | inntektsperiodeTom |
      | LOENNSINNTEKT | 10000 | 2020-03 | 2020-03-20         | 2020-04-10         |
      | LOENNSINNTEKT | 10000 | 2020-04 |                    |                    |
    Og avtale med arbeidsgiveravgift "0.141", feriepengersats "0.12"
    Når lønnstilskudd på 40 prosent skal refunderes for periode "2020-03-20" til "2020-04-10"
    Så beregnes refusjon til "7110" kr for periode

  Scenario: Beregn nedsatt arbeidsevne lønnstilskudd for deler av perioden med inntektsperiode dagpengersats
    Gitt følgende opplysninger om inntekt
      | inntektType  | beløp | måned   | inntektsperiodeFom | inntektsperiodeTom |
      | LOENNSINNTEKT | 10000 | 2020-03 | 2020-03-20            | 2020-04-10            |
      | LOENNSINNTEKT | 20000 | 2020-03 | 2020-03-20            | 2020-04-10            |
    Og avtale med arbeidsgiveravgift "0.141", feriepengersats "0.12"
    Når lønnstilskudd på 60 prosent skal refunderes for periode "2020-03-20" til "2020-04-10"
    Så beregnes refusjon til "23463" kr for periode

  Scenario: Beregn nedsatt arbeidsevne lønnstilskudd for ulike perioder med inntektsperiode dagpengersats
    Gitt følgende opplysninger om inntekt
      | inntektType  | beløp | måned   | inntektsperiodeFom | inntektsperiodeTom |
      | LOENNSINNTEKT | 10000 | 2020-01 | 2020-01-20            | 2020-01-31            |
      | LOENNSINNTEKT | 20000 | 2020-01 | 2020-01-01            | 2020-01-31            |
    Og avtale med arbeidsgiveravgift "0.141", feriepengersats "0.12"
    Når lønnstilskudd på 60 prosent skal refunderes for periode "2020-01-01" til "2020-01-31"
    Så beregnes refusjon til "23463" kr for periode

  Scenario: Beregn nedsatt arbeidsevne lønnstilskudd for ulike perioder med inntektsperiode dagpengersats med en gap mellom inntektene
    Gitt følgende opplysninger om inntekt
      | inntektType  | beløp | måned   | inntektsperiodeFom | inntektsperiodeTom |
      | LOENNSINNTEKT | 10000 | 2020-04 | 2020-04-01            | 2020-04-10            |
      | FERIE        | 5000  | 2020-04 | 2020-04-11            | 2020-04-17            |
      | LOENNSINNTEKT | 20000 | 2020-04 | 2020-04-18            | 2020-04-30            |
    Og avtale med arbeidsgiveravgift "0.141", feriepengersats "0.12"
    Når lønnstilskudd på 60 prosent skal refunderes for periode "2020-01-04" til "2020-04-30"
    Så beregnes refusjon til "23463" kr for periode

  Scenario: Beregn nedsatt arbeidsevne lønnstilskudd for hele perioden med eldre dato og ny ytelse
    Gitt følgende opplysninger om inntekt
      | inntektType  | beløp | måned   | inntektsperiodeFom | inntektsperiodeTom |
      | LOENNSINNTEKT | 7000  | 2019-01 |  2019-01-01           |     2019-01-31        |
      | YTELSE       | 2000  | 2020-01 |                       |                       |
    Og avtale med arbeidsgiveravgift "0.141", feriepengersats "0.12"
    Når lønnstilskudd på 60 prosent skal refunderes for periode "2020-01-01" til "2020-01-31"
    Så beregnes refusjon til "0" kr for periode