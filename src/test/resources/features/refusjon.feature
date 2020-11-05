#language: no
Egenskap: Beregn refusjon for lønnstilskudd

  Scenario: Beregn lønnstilskudd for deler av perioden med inntektsperiode dagpengersats samt OTP, Feriepenger og AV.avgift
    Gitt følgende opplysninger om inntekt
      | inntektType  | beløp | måned   | inntektsperiodeFom | inntektsperiodeTom |
      | LØNNSINNTEKT | 20000 | 2020-01 |                    |                    |
    Og avtale med arbeidsgiveravgift "0.141", feriepengersats "0.12" og OTP "0.02"
    Når lønnstilskudd på 60 prosent skal refunderes for periode "2020-01-01" til "2020-01-31"
    Så beregnes refusjon til "15642" kr per måned

  Scenario: Beregn lønnstilskudd for deler av perioden med inntektsperiode dagpengersats
    Gitt følgende opplysninger om inntekt
      | inntektType  | beløp | måned   | inntektsperiodeFom | inntektsperiodeTom |
      | LØNNSINNTEKT | 10000 | 2020-03 | 2020-03-20         | 2020-04-10         |
      | LØNNSINNTEKT | 10000 | 2020-04 |                    |                    |
    Og avtale med arbeidsgiveravgift "0.141", feriepengersats "0.12" og OTP "0.02"
    Når lønnstilskudd på 40 prosent skal refunderes for periode "2020-03-20" til "2020-04-10"
    Så beregnes refusjon til "7110" kr per måned

  Scenario: Beregn nedsatt arbeidsevne lønnstilskudd for deler av perioden med inntektsperiode dagpengersats
    Gitt følgende opplysninger om inntekt
      | inntektType  | beløp | måned   | inntektsperiodeFom | inntektsperiodeTom |
      | LØNNSINNTEKT | 10000 | 2020-03 | 2020-03-20            | 2020-04-10            |
      | LØNNSINNTEKT | 20000 | 2020-03 | 2020-03-20            | 2020-04-10            |
    Og avtale med arbeidsgiveravgift "0.141", feriepengersats "0.12" og OTP "0.02"
    Når lønnstilskudd på 60 prosent skal refunderes for periode "2020-03-20" til "2020-04-10"
    Så beregnes refusjon til "23463" kr per måned

  Scenario: Beregn nedsatt arbeidsevne lønnstilskudd for ulike perioder med inntektsperiode dagpengersats
    Gitt følgende opplysninger om inntekt
      | inntektType  | beløp | måned   | inntektsperiodeFom | inntektsperiodeTom |
      | LØNNSINNTEKT | 10000 | 2020-01 | 2020-01-20            | 2020-01-31            |
      | LØNNSINNTEKT | 20000 | 2020-01 | 2020-01-01            | 2020-01-31            |
    Og avtale med arbeidsgiveravgift "0.141", feriepengersats "0.12" og OTP "0.02"
    Når lønnstilskudd på 60 prosent skal refunderes for periode "2020-01-01" til "2020-01-31"
    Så beregnes refusjon til "23463" kr per måned

  Scenario: Beregn nedsatt arbeidsevne lønnstilskudd for ulike perioder med inntektsperiode dagpengersats med en gap mellom inntektene
    Gitt følgende opplysninger om inntekt
      | inntektType  | beløp | måned   | inntektsperiodeFom | inntektsperiodeTom |
      | LØNNSINNTEKT | 10000 | 2020-01 | 2020-01-01            | 2020-01-10            |
      | FERIE        |   0   | 2020-01 | 2020-01-11            | 2020-01-17            |
      | LØNNSINNTEKT | 20000 | 2020-01 | 2020-01-18            | 2020-01-31            |
    Og avtale med arbeidsgiveravgift "0.141", feriepengersats "0.12" og OTP "0.02"
    Når lønnstilskudd på 60 prosent skal refunderes for periode "2020-01-01" til "2020-01-31"
    Så beregnes refusjon til "23463" kr per måned

  Scenario: Beregn nedsatt arbeidsevne lønnstilskudd for hele perioden med eldre dato og ny ytelse
    Gitt følgende opplysninger om inntekt
      | inntektType  | beløp | måned   | inntektsperiodeFom | inntektsperiodeTom |
      | LØNNSINNTEKT | 7000  | 2019-01 |  2019-01-01           |     2019-01-31        |
      | YTELSE       | 2000  | 2020-01 |                       |                       |
    Og avtale med arbeidsgiveravgift "0.141", feriepengersats "0.12" og OTP "0.02"
    Når lønnstilskudd på 60 prosent skal refunderes for periode "2020-01-01" til "2020-01-31"
    Så beregnes refusjon til "0" kr per måned