create table avtalegrunnlag
(
    id                      varchar primary key,
    avtale_id               varchar,
    tilskuddsperiode_id     varchar,
    deltaker_fornavn        varchar,
    deltaker_etternavn      varchar,
    deltaker_fnr            varchar(11),
    veileder_nav_ident      varchar,
    bedrift_navn            varchar,
    bedrift_nr              varchar(9),
    tilskudd_fra_dato       date,
    tilskudd_til_dato       date,
    feriepenger_sats        numeric(4, 3),
    otp_sats                numeric(4, 3),
    arbeidsgiveravgift_sats numeric(4, 3),
    tiltakstype             varchar,
    tilskudd_beløp          numeric
);

create table refusjonsak
(
    id                        varchar primary key,
    avtalegrunnlag_id         varchar references avtalegrunnlag (id),
    inntektsgrunnlag_id       varchar references inntektsgrunnlag (id),
    refusjonsbeløp            numeric,
    status                    varchar,
    godkjent_av_arbeidsgiver  timestamp without time zone,
    godkjent_av_saksbehandler timestamp without time zone
);

create table inntektsgrunnlag
(
    id                  varchar primary key,
    innhentet_tidspunkt timestamp without time zone
);

create table inntektslinje
(
    id                     varchar primary key,
    inntektsgrunnlag_id    varchar references inntektsgrunnlag (id),
    type                   varchar,
    beløp                  numeric,
    måned                  varchar,
    opptjeningsperiode_fom date,
    opptjeningsperiode_tom date
);