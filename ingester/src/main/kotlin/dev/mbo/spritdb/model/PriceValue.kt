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

package dev.mbo.spritdb.model

import dev.mbo.spritdb.model.base.KafkaValue
import dev.mbo.spritdb.model.base.TopicSchema
import java.math.BigDecimal

@TopicSchema(name = "price", version = 1)
data class PriceValue(
    val id: Long,
    val lat: Double,
    val lon: Double,
    val name: String,
    val fuelType: String,
    val time: Long,
    val price: BigDecimal,
) : KafkaValue