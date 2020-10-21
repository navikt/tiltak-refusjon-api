#language: no
Egenskap: Beregn refusjon for lønnstilskudd

  Scenario: Beregn lønnstilskudd for hele perioden
    Gitt følgende opplysninger om inntekt
      | inntektType  | beløp | måned   | opptjeningsperiodeFom | opptjeningsperiodeTom |
      | LØNNSINNTEKT | 10000 | 2020-01 | 2020-01-01            | 2020-01-31            |
    Når lønnstilskudd på 60 prosent skal refunderes for periode "2020-01-01" til "2020-01-31"
    Så beregnes refusjon til "6000" kr

  Scenario: Beregn lønnstilskudd for hele perioden uten opptjeningsperiode
    Gitt følgende opplysninger om inntekt
      | inntektType  | beløp | måned   | opptjeningsperiodeFom | opptjeningsperiodeTom |
      | LØNNSINNTEKT | 10000 | 2020-01 |                       |                       |
    Når lønnstilskudd på 60 prosent skal refunderes for periode "2020-01-01" til "2020-01-31"
    Så beregnes refusjon til "6000" kr

  Scenario: Beregn lønnstilskudd for deler av perioden
    Gitt følgende opplysninger om inntekt
      | inntektType  | beløp | måned   | opptjeningsperiodeFom | opptjeningsperiodeTom |
      | LØNNSINNTEKT | 10000 | 2020-01 |                       |                       |
    Når lønnstilskudd på 60 prosent skal refunderes for periode "2020-01-01" til "2020-01-15"
#    Så er det ikke mulig å beregne refusjon
    Så beregnes refusjon til "6000" kr

  @skip_scenario
  Scenario: Beregn lønnstilskudd for deler av perioden med opptjeningsperiode
    Gitt følgende opplysninger om inntekt
      | inntektType  | beløp | måned   | opptjeningsperiodeFom | opptjeningsperiodeTom |
      | LØNNSINNTEKT | 10000 | 2020-01 | 2020-01-01            | 2020-01-15            |
    Når lønnstilskudd på 60 prosent skal refunderes for periode "2020-01-01" til "2020-01-10"
    Så beregnes refusjon til "4000" kr

  @skip_scenario
  Scenario: Beregn lønnstilskudd for hele perioden med feil måneder
    Gitt følgende opplysninger om inntekt
      | inntektType  | beløp | måned   | opptjeningsperiodeFom | opptjeningsperiodeTom |
      | LØNNSINNTEKT | 7000  | 2019-01 |                       |                       |
      | YTELSE       | 2000  | 2020-01 |                       |                       |
    Når lønnstilskudd på 60 prosent skal refunderes for periode "2020-01-01" til "2020-01-31"
    Så beregnes refusjon til "0" kr

