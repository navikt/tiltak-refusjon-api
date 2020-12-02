drop table refusjon;

create table tilskuddsgrunnlag
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
    tilskudd_fom            date,
    tilskudd_tom            date,
    feriepenger_sats        numeric(4, 3),
    otp_sats                numeric(4, 3),
    arbeidsgiveravgift_sats numeric(4, 3),
    tiltakstype             varchar,
    tilskuddsbeløp          numeric,
    lønnstilskuddsprosent   integer
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
    inntekt_type           varchar,
    beløp                  numeric,
    måned                  varchar,
    opptjeningsperiode_fom date,
    opptjeningsperiode_tom date
);

create table refusjon
(
    id                        varchar primary key,
    tilskuddsgrunnlag_id      varchar references tilskuddsgrunnlag (id),
    inntektsgrunnlag_id       varchar references inntektsgrunnlag (id),
    status                    varchar,
    godkjent_av_arbeidsgiver  timestamp without time zone,
    godkjent_av_saksbehandler timestamp without time zone,
    refusjonsbeløp            numeric,
    commit_hash               varchar,
    deltaker_fnr              varchar(11),
    bedrift_nr                varchar(9)
);