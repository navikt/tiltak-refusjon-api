create table korreksjon(
    id varchar primary key,
    refusjon_id varchar references refusjon(id),
    korreksjonsnummer integer,
    inntektsgrunnlag_id varchar references inntektsgrunnlag(id),
    beregning_id varchar references beregning(id),
    kostnadssted varchar,
    bedrift_kontonummer varchar,
    inntekter_kun_fra_tiltaket boolean,
    endret_brutto_lønn numeric,
    godkjent_tidspunkt timestamp without time zone,
    godkjent_av_nav_ident varchar,
    besluttet_av_nav_ident varchar,
    status varchar
);

create table korreksjon_korreksjonsgrunner
(
    korreksjon_id  varchar references korreksjon(id),
    korreksjonsgrunner varchar,
    primary key (korreksjon_id, korreksjonsgrunner)
);