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

package dev.mbo.spritdb.service

import com.fasterxml.jackson.databind.ObjectMapper
import dev.mbo.spritdb.model.PriceKey
import dev.mbo.spritdb.model.PriceValue
import dev.mbo.spritdb.model.base.Schema
import dev.mbo.spritdb.nowMillis
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.header.Header
import org.apache.kafka.common.header.internals.RecordHeader
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.SendResult
import org.springframework.stereotype.Service
import org.springframework.util.concurrent.ListenableFuture

@Service
class PriceSender(
    private val kafkaTemplate: KafkaTemplate<String, String>,
    private val objectMapper: ObjectMapper,
    @Value("\${application.kafka.topic}") private val kafkaTopic: String,
) : SenderService<PriceKey, PriceValue> {

    companion object {
        private val SCHEMA = Schema.getSchemaData(PriceKey::class, PriceValue::class)
        private val HEADERS = listOf(
            RecordHeader("schema", SCHEMA.name.toByteArray()),
            RecordHeader("schema_version", SCHEMA.version.toString().toByteArray()),
        )
    }

    private val log = LoggerFactory.getLogger(javaClass)

    override fun send(
        key: PriceKey,
        value: PriceValue,
        extraHeaders: List<Header>
    ): ListenableFuture<SendResult<String, String>> {
        log.debug("send price update to {}: {}, {}", kafkaTopic, key, value)
        val combinedHeaders = ArrayList<Header>(HEADERS.size + extraHeaders.size)
        combinedHeaders.addAll(HEADERS)
        combinedHeaders.addAll(extraHeaders)

        return kafkaTemplate.send(
            ProducerRecord(
                kafkaTopic,
                null,
                nowMillis,
                objectMapper.writeValueAsString(key),
                objectMapper.writeValueAsString(value),
                combinedHeaders
            )
        )
    }

}