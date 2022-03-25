alter table inntektslinje add column er_opptjent_i_periode boolean default null;
update inntektslinje set er_opptjent_i_periode = true where id in (
        select i.id from inntektslinje i
        inner join refusjonsgrunnlag rg on rg.inntektsgrunnlag_id = i.inntektsgrunnlag_id
        inner join refusjon r on r.refusjonsgrunnlag_id = rg.id
        where r.godkjent_av_arbeidsgiver is not null);