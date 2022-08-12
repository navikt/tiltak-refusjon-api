package no.nav.arbeidsgiver.tiltakrefusjon.refusjon

import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext


class RefusjonRepositoryImpl: RefusjonRepositoryCustom {

    @PersistenceContext
    lateinit var entityManager: EntityManager

    fun checkMaxSublistIndex(inputIndex: Int, listSize: Int): Int {
        return if (inputIndex > listSize) listSize else inputIndex
    }

    fun getRefusjonerSublist(refusjoner: List<Refusjon>, pageable: Pageable): List<Refusjon> {
        return refusjoner.subList(
            pageable.pageSize * pageable.pageNumber,
            checkMaxSublistIndex(
                pageable.pageSize * (pageable.pageNumber + 1),
                refusjoner.size
            )
        )
    }

    fun checkAndAppendStatus(sql: StringBuilder, params: HashMap<String, Any>, status: RefusjonStatus?){
        if (status != null) {
            sql.append(" and r.status = :status")
            params["status"] = status
        }
    }

    fun checkAndAppendTiltakstype(sql: StringBuilder, params: HashMap<String, Any>, tiltakstype: Tiltakstype?){
        if(tiltakstype != null) {
            sql.append(" and t.tiltakstype = :tiltakstype")
            params["tiltakstype"] = tiltakstype
        }
    }

    fun setDefaultSorting(sql: StringBuilder) =
        sql.append(" order by (CASE WHEN r.status = 'KLAR_FOR_INNSENDING' THEN 0 else 1 END)")

    fun checkAndAppendSoringOrder(sql: StringBuilder, params: HashMap<String, Any>, sortingOrder: String?){
        if(sortingOrder != null) {
            when (sortingOrder) {
                else -> setDefaultSorting(sql)
            }
        }
        setDefaultSorting(sql)
    }

    override fun findAllByBedriftNrAndStatusSorted(
        bedrift_nr: List<String>,
        status: RefusjonStatus?,
        tiltakstype: Tiltakstype?,
        sortingOrder: String?,
        pageable: Pageable
    ): Page<Refusjon> {
        val params:HashMap<String, Any> = HashMap<String,Any>()
        val sql = StringBuilder()

        sql.append("select * from Refusjon r " +
                "join Refusjonsgrunnlag rg on r.refusjonsgrunnlag_id = rg.id " +
                "join tilskuddsgrunnlag t on rg.tilskuddsgrunnlag_id = t.id " +
                "where t.bedrift_nr in (:bedrift_nr)")
        params["bedrift_nr"] = bedrift_nr

        checkAndAppendStatus(sql, params, status)
        checkAndAppendTiltakstype(sql, params, tiltakstype)
        checkAndAppendSoringOrder(sql, params, sortingOrder)

        val query = entityManager.createNativeQuery(sql.toString(), Refusjon::class.java)
        for ((key, value) in params) {
            query.setParameter(key, value)
        }
        val refusjoner: List<Refusjon> = (query.resultList as List<Refusjon>)
        val refusjonerSublist = getRefusjonerSublist(refusjoner, pageable)

        return PageImpl(refusjonerSublist, pageable, refusjoner.size.toLong())
    }

}