-- sist_endret-feltet har ikke tidssone i databasen, men er en instant i jpa-entiteten.
-- Instants er tidspunkter med tidssoner, så kolonnen burde også ha tidssone.
alter table refusjon alter column sist_endret type timestamp with time zone
    using sist_endret at time zone 'UTC';
