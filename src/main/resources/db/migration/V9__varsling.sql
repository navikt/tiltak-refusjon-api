create table varsling
(
    id                    varchar primary key,
    refusjon_id           varchar references refusjon (id),
    varsel_type           varchar,
    varsel_tidspunkt      timestamp without time zone
);