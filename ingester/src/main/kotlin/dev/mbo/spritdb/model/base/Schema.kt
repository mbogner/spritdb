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

package dev.mbo.spritdb.model.base

import kotlin.reflect.KClass

data class Schema(
    val name: String,
    val version: Int
) {
    companion object {
        fun <K : KafkaKey, V : KafkaValue> getSchemaData(keyClass: KClass<K>, valueClass: KClass<V>): Schema {
            val key = findSchemaVersion(keyClass)
            val value = findSchemaVersion(valueClass)
            compareSchema(key.name, value.name, "schema")
            compareSchema(key.version, value.version, "version")
            return Schema(name = key.name, version = key.version)
        }

        private fun compareSchema(k: Any, v: Any, name: String) {
            if (k != v) {
                throw IllegalStateException("key $name $k is not the same as $name $v")
            }
        }

        private fun findSchemaVersion(clazz: KClass<*>): TopicSchema =
            clazz.annotations.find { it.annotationClass == TopicSchema::class } as? TopicSchema
                ?: throw IllegalStateException(
                    "${TopicSchema::class.simpleName} annotation not present on type ${clazz.simpleName}"
                )
    }
}
