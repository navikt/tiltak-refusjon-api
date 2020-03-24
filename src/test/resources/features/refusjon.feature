#language: no
Egenskap: Beregn refusjon for lønnstilskudd

  Scenario: Beregn lønnstilskudd for hele perioden
    Gitt følgende opplysninger om inntekt
      | inntektType  | beløp | opptjeningsperiodeFom | opptjeningsperiodeTom |
      | LØNNSINNTEKT | 10000 | 2020-01-01            | 2020-01-31            |
    Når lønnstilskudd på 60 prosent skal refunderes for periode "2020-01-01" til "2020-01-31"
    Så beregnes refusjon til "6000" kr

  Scenario: Beregn lønnstilskudd for deler av perioden
    Gitt følgende opplysninger om inntekt
      | inntektType  | beløp | opptjeningsperiodeFom | opptjeningsperiodeTom |
      | LØNNSINNTEKT | 10000 | 2020-01-01            | 2020-01-31            |
    Når lønnstilskudd på 60 prosent skal refunderes for periode "2020-01-01" til "2020-01-15"
    Så beregnes refusjon til "3000" kr