package org.jesperancinha.concert.buy.oyc.commons.domain

import io.micronaut.data.annotation.*
import io.micronaut.data.annotation.Relation.Cascade.PERSIST
import io.micronaut.data.annotation.Relation.Kind.ONE_TO_ONE
import io.micronaut.data.model.naming.NamingStrategies
import io.micronaut.data.model.naming.NamingStrategies.UnderScoreSeparatedLowerCase
import io.micronaut.data.model.query.builder.sql.Dialect
import io.micronaut.data.r2dbc.annotation.R2dbcRepository
import io.micronaut.data.repository.jpa.kotlin.CoroutineJpaSpecificationExecutor
import io.micronaut.data.repository.kotlin.CoroutineCrudRepository
import java.time.LocalDateTime
import java.util.*

/**
 * Created by jofisaes on 30/03/2022
 */
@MappedEntity(value = "receipt", namingStrategy = UnderScoreSeparatedLowerCase::class)
data class Receipt(
    @field: Id
    @field: AutoPopulated
    var id: UUID? = null,
    @field: AutoPopulated
    var reference: UUID? = null,
    @field:DateCreated
    var createdAt: LocalDateTime? = null,
    @field: Relation(value = ONE_TO_ONE, cascade = [PERSIST])
    val ticketReservation: TicketReservation? = null
)

@R2dbcRepository(dialect = Dialect.POSTGRES)
interface ReceiptRepository : CoroutineCrudRepository<Receipt, UUID>,
    CoroutineJpaSpecificationExecutor<Receipt>
