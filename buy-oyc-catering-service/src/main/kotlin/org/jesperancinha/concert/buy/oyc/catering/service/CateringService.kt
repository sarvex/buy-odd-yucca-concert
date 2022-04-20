package org.jesperancinha.concert.buy.oyc.catering.service

import io.lettuce.core.RedisClient
import io.lettuce.core.pubsub.RedisPubSubAdapter
import io.lettuce.core.pubsub.api.async.RedisPubSubAsyncCommands
import io.micronaut.context.annotation.Factory
import jakarta.inject.Singleton
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.jesperancinha.concert.buy.oyc.commons.domain.*
import org.jesperancinha.concert.buy.oyc.commons.dto.DrinkDto
import org.jesperancinha.concert.buy.oyc.commons.dto.MealDto
import org.jesperancinha.concert.buy.oyc.commons.dto.toData
import org.jesperancinha.concert.buy.oyc.commons.dto.toDto
import org.jesperancinha.concert.buy.oyc.commons.pubsub.initPubSub
import java.io.ObjectInputStream
import javax.validation.Valid

private const val DRINK_CHANNEL = "drinkChannel"

private const val MEAL_CHANNEL = "mealChannel"

/**
 * Created by jofisaes on 20/04/2022
 */

@DelicateCoroutinesApi
@Singleton
class CateringService(
    private val drinkReservationRepository: DrinkReservationRepository,
    private val mealReservationRepository: MealReservationRepository,
    redisClient: RedisClient,
    private val pubSubDrinkCommands: RedisPubSubAsyncCommands<String, DrinkDto>,
    private val pubSubMealCommands: RedisPubSubAsyncCommands<String, MealDto>,
    drinkRepository: DrinkRepository,
    mealRepository: MealRepository,
    ticketRepository: TicketRepository,

    ) {

    init {
        redisClient.initPubSub(
            channelName = DRINK_CHANNEL,
            redisCodec = DrinkCodec(),
            redisPubSubAdapter = DrinkListener(
                drinkReservationRepository,
                drinkRepository,
                ticketRepository
            )
        )
        redisClient.initPubSub(
            channelName = MEAL_CHANNEL,
            redisCodec = MealCodec(),
            redisPubSubAdapter = MealListener(
                mealReservationRepository,
                mealRepository,
                ticketRepository
            )
        )
    }

    suspend fun createDrink(drinkDto: @Valid DrinkDto?): Unit =
        withContext(Dispatchers.Default) {
            pubSubDrinkCommands.publish(DRINK_CHANNEL, drinkDto)
        }

    suspend fun createMeal(mealDto: @Valid MealDto?): Unit =
        withContext(Dispatchers.Default) {
            pubSubMealCommands.publish(MEAL_CHANNEL, mealDto)
        }

    fun getAllDrinks(): Flow<DrinkDto> = drinkReservationRepository.findAll().map { it.toDto }

    fun getAllMeals(): Flow<MealDto> = mealReservationRepository.findAll().map { it.toDto }
}

@Factory
class RedisBeanFactory {
    @Singleton
    fun pubSubDrinkCommands(redisClient: RedisClient): RedisPubSubAsyncCommands<String, DrinkDto>? =
        redisClient.connectPubSub(DrinkCodec()).async()

    @Singleton
    fun pubSubMealCommands(redisClient: RedisClient): RedisPubSubAsyncCommands<String, MealDto>? =
        redisClient.connectPubSub(MealCodec()).async()
}

@DelicateCoroutinesApi
class DrinkListener(
    private val drinkReservationRepository: DrinkReservationRepository,
    private val drinkRepository: DrinkRepository,
    private val ticketRepository: TicketRepository,
) : RedisPubSubAdapter<String, DrinkDto>() {
    override fun message(key: String, drinkDto: DrinkDto) {
        CoroutineScope(Dispatchers.IO).launch {
            val parkingReservation =
                drinkRepository.findById(drinkDto.drinkId ?: throw RuntimeException("Drink Not Sent"))?.let { drink ->
                    drinkDto.toData(
                        ticketRepository.findById(
                            drinkDto.ticketReservationId ?: throw RuntimeException("Ticket Not Sent")
                        ),
                        drink
                    )
                } ?:  throw RuntimeException("Drink Not Found")
            drinkReservationRepository.save(parkingReservation)
        }
    }
}

@DelicateCoroutinesApi
class MealListener(
    private val mealReservationRepository: MealReservationRepository,
    private val mealRepository: MealRepository,
    private val ticketRepository: TicketRepository,
    ) : RedisPubSubAdapter<String, MealDto>() {
    override fun message(key: String, mealDto: MealDto) {
        CoroutineScope(Dispatchers.IO).launch {
        val parkingReservation =
            mealRepository.findById(mealDto.mealId ?: throw RuntimeException("Meal Not Sent"))?.let { meal ->
                mealDto.toData(
                    ticketRepository.findById(
                        mealDto.ticketReservationId ?: throw RuntimeException("Ticket Not Sent")
                    ),
                    meal
                )
            } ?:  throw RuntimeException("Meal Not Found")
            mealReservationRepository.save(parkingReservation)
        }
    }
}

class DrinkCodec : BuyOycCodec<DrinkDto>() {
    override fun readCodecObject(it: ObjectInputStream): DrinkDto = it.readTypedObject()
}

class MealCodec : BuyOycCodec<MealDto>() {
    override fun readCodecObject(it: ObjectInputStream): MealDto = it.readTypedObject()
}
