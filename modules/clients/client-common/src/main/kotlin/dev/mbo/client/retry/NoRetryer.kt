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

package dev.mbo.client.retry

import feign.RetryableException
import feign.Retryer
import org.apache.http.HttpStatus

@Suppress("unused")
class NoRetryer : Retryer {

    // we only need one instance for throwing exceptions
    private val instance: NoRetryer
        get() = this

    override fun clone(): Retryer {
        return instance
    }

    override fun continueOrPropagate(exc: RetryableException) {
        throw RetryDisabledException(HttpStatus.SC_INTERNAL_SERVER_ERROR, "retry disabled", exc.cause)
    }
}