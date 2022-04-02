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

import dev.mbo.spritdb.model.base.KafkaKey
import dev.mbo.spritdb.model.base.KafkaValue
import org.apache.kafka.common.header.Header
import org.springframework.kafka.support.SendResult
import org.springframework.util.concurrent.ListenableFuture

interface SenderService<K : KafkaKey, V : KafkaValue> {

    /**
     * Send key-value pair. We want to use custom serializer and deserializer to avoid unnecessary data and therefor
     * use String as key and value on the topic but both follow JSON annotation in Kafka. Headers are added by the
     * implementation.
     */
    fun send(
        key: K,
        value: V,
        extraHeaders: List<Header> = emptyList()
    ): ListenableFuture<SendResult<String, String>>

}