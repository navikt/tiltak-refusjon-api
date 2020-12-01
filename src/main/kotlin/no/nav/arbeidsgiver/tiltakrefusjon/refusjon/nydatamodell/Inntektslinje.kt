package no.nav.arbeidsgiver.tiltakrefusjon.refusjon.nydatamodell

import com.fasterxml.jackson.annotation.JsonIgnore
import com.github.guepardoapps.kulid.ULID
import java.time.LocalDate
import java.time.YearMonth
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne

@Entity
data class InntektslinjeEntity(
        @Id
        val id: String = ULID.random(),
        @ManyToOne
        @JoinColumn(name = "inntektsgrunnlag_id")
        @JsonIgnore
        val inntektsgrunnlag: Inntektsgrunnlag,
        val inntektType: String,
        val beløp: Double,
        val måned: YearMonth,
        val opptjeningsperiodeFom: LocalDate,
        val opptjenningsperiodeTom: LocalDate
)