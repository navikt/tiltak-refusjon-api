-- godkjent_av_arbeidsgiver-feltet har ikke tidssone i databasen, men er en instant i jpa-entiteten.
-- Instants er tidspunkter med tidssoner, så kolonnen burde også ha tidssone.
alter table refusjon alter column utbetalt_tidspunkt type timestamp with time zone
    using utbetalt_tidspunkt at time zone 'UTC';
