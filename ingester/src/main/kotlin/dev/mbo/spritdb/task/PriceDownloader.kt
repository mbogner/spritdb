/*
 * Copyright 2022 mbodev @ https://mbo.dev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.mbo.spritdb.task

import dev.mbo.spritdb.client.spritpreisrechner.api.SearchApi
import dev.mbo.spritdb.client.spritpreisrechner.model.PriceDto
import dev.mbo.spritdb.config.CustomConfig
import dev.mbo.spritdb.model.PriceKey
import dev.mbo.spritdb.model.PriceValue
import dev.mbo.spritdb.nowMillis
import dev.mbo.spritdb.service.PriceSender
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class PriceDownloader(
    private val priceSender: PriceSender,
    private val searchApi: SearchApi,
    private val customConfig: CustomConfig,
) {
    companion object {
        private const val DIESEL = "DIE"
        private const val SUPER = "SUP"
        private val FUEL_TYPES = setOf(DIESEL, SUPER)
    }

    private val log = LoggerFactory.getLogger(javaClass)

    @Scheduled(cron = "\${application.tasks.priceDownloader.cron}")
    fun downloadPrices() {
        log.info("run scheduled price update")
        if (null == customConfig.areas || customConfig.areas!!.isEmpty()) {
            throw IllegalStateException("missing config application.config.areas")
        }
        try {
            for (area in customConfig.areas!!) {
                log.info("updating {}", area)
                for (fuelType in FUEL_TYPES) {
                    val response = searchApi.searchGasStationsByAddressUsingGET(
                        latitude = area.lat!!,
                        longitude = area.lon!!,
                        fuelType = fuelType,
                        includeClosed = true
                    )
                    if (HttpStatus.OK != response.statusCode) {
                        log.warn("bad status code in response: {}", response)
                        return
                    }
                    if (null == response.body) {
                        log.warn("response body was null: {}", response)
                        return
                    }

                    for (gasStationDto in response.body!!) {
                        if (null == gasStationDto.id ||
                            null == gasStationDto.location ||
                            null == gasStationDto.location!!.latitude ||
                            null == gasStationDto.location!!.longitude
                        ) {
                            log.warn("gas station ignored: {}", gasStationDto)
                            continue
                        }
                        val price: PriceDto? = gasStationDto.prices?.find { it.fuelType == fuelType }
                        if (null == price?.amount) {
                            log.debug(
                                "no price for {} - skipping {} ({}, {})",
                                fuelType,
                                gasStationDto.id,
                                gasStationDto.name,
                                gasStationDto.location
                            )
                            continue
                        }
                        log.debug(
                            "price update for {}={} station {} ({}, {})",
                            fuelType,
                            price.amount,
                            gasStationDto.id,
                            gasStationDto.name,
                            gasStationDto.location
                        )
                        priceSender.send(
                            PriceKey(id = gasStationDto.id!!, fuelType = fuelType),
                            PriceValue(
                                id = gasStationDto.id!!,
                                lat = gasStationDto.location!!.latitude!!,
                                lon = gasStationDto.location!!.longitude!!,
                                name = gasStationDto.name,
                                fuelType = fuelType,
                                time = nowMillis,
                                price = price.amount!!
                            )
                        )
                    }
                }
            }

        } catch (exc: Exception) {
            log.error("request failed", exc)
        }
    }

}