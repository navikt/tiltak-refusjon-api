alter table refusjon
    rename column innhentet_bedrift_kontonummer_tidspunkt to bedrift_kontonummer_innhentet_tidspunkt;

create table refusjonsgrunnlag
(
    id                                      varchar primary key,
    tilskuddsgrunnlag_id                    varchar references tilskuddsgrunnlag (id),
    inntektsgrunnlag_id                     varchar references inntektsgrunnlag (id),
    beregning_id                            varchar references beregning (id),
    bedrift_kontonummer                     varchar,
    bedrift_kontonummer_innhentet_tidspunkt timestamp without time zone,
    inntekter_kun_fra_tiltaket              boolean,
    endret_brutto_lønn                      numeric,
    tidligere_utbetalt                      numeric
);

alter table refusjon
    add column refusjonsgrunnlag_id varchar references refusjonsgrunnlag (id);


create table korreksjon
(
    id                     varchar primary key,
    korrigerer_refusjon_id varchar references refusjon (id),
    refusjonsgrunnlag_id   varchar references refusjonsgrunnlag (id),
    korreksjonsnummer      integer,
    deltaker_fnr           varchar(11),
    bedrift_nr             varchar(9),
    kostnadssted           varchar,
    godkjent_tidspunkt     timestamp without time zone,
    godkjent_av_nav_ident  varchar,
    besluttet_av_nav_ident varchar,
    besluttet_tidspunkt    timestamp without time zone,
    status                 varchar
);

create table korreksjon_korreksjonsgrunner
(
    korreksjon_id      varchar references korreksjon (id),
    korreksjonsgrunner varchar,
    primary key (korreksjon_id, korreksjonsgrunner)
);

alter table refusjon
    add column korreksjon_id varchar references korreksjon (id);