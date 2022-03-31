alter table refusjonsgrunnlag add column fratrekk_refunderbar_beløp boolean default null;
alter table refusjonsgrunnlag add column refunderbar_beløp numeric default null;

alter table beregning add column tidligere_refundert_beløp numeric default 0;
alter table beregning add column sum_utgifter_fratrukket_refundert_beløp numeric default 0;

update beregning set tidligere_refundert_beløp = 0 where tidligere_refundert_beløp is null;
update beregning set sum_utgifter_fratrukket_refundert_beløp = 0 where sum_utgifter_fratrukket_refundert_beløp is null;