package org.jesperancinha.concert.buy.oyc.commons.domain

import io.micronaut.data.annotation.AutoPopulated
import io.micronaut.data.annotation.DateCreated
import io.micronaut.data.annotation.Id
import io.micronaut.data.annotation.MappedEntity
import io.micronaut.data.model.naming.NamingStrategies
import io.micronaut.data.model.query.builder.sql.Dialect
import io.micronaut.data.r2dbc.annotation.R2dbcRepository
import io.micronaut.data.repository.jpa.kotlin.CoroutineJpaSpecificationExecutor
import io.micronaut.data.repository.kotlin.CoroutineCrudRepository
import java.time.LocalDateTime
import java.util.*

/**
 * Created by jofisaes on 19/12/2021
 */
@MappedEntity(value = "parking_reservation", namingStrategy = NamingStrategies.UnderScoreSeparatedLowerCase::class)
data class Ticket(
    @field: Id
    @field: AutoPopulated
    val id: UUID? = null,
    @field:DateCreated
    val createdAt: LocalDateTime? = LocalDateTime.now(),
)

@R2dbcRepository(dialect = Dialect.POSTGRES)
interface TicketRepository : CoroutineCrudRepository<Ticket, UUID>,
    CoroutineJpaSpecificationExecutor<Ticket>