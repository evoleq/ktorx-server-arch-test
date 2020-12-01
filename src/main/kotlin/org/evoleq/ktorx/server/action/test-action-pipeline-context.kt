/**
 * Copyright (c) 2020 Dr. Florian Schmidt
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
package org.evoleq.ktorx.server.action

import io.ktor.application.*
import io.ktor.server.testing.*
import io.ktor.util.pipeline.*
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlin.coroutines.CoroutineContext

@Suppress("FunctionName")
fun TestActionPipelineContext(
    call: ApplicationCall,
    coroutineContext: CoroutineContext
): PipelineContext<Unit, ApplicationCall> = object : PipelineContext<Unit, ApplicationCall> {
    /**
     * Object representing context in which pipeline executes
     */
    override val context: ApplicationCall
        get() = call

    /**
     * The context of this scope.
     * Context is encapsulated by the scope and used for implementation of coroutine builders that are extensions on the scope.
     * Accessing this property in general code is not recommended for any purposes except accessing the [Job] instance for advanced usages.
     *
     * By convention, should contain an instance of a [job][Job] to enforce structured concurrency.
     */
    override val coroutineContext: CoroutineContext
        get() = coroutineContext

    /**
     * Subject of this pipeline execution that goes along the pipeline
     */
    override val subject: Unit
        get() = Unit

    /**
     * Finishes current pipeline execution
     */
    override fun finish() { }

    /**
     * Continues execution of the pipeline with the same subject
     */
    override suspend fun proceed() { }

    /**
     * Continues execution of the pipeline with the given subject
     */
    override suspend fun proceedWith(subject: Unit) { }

}

suspend fun <T> withActionTestContext(
    call: TestApplicationEngine.()->ApplicationCall = {createCall{}},
    action:  suspend PipelineContext<Unit, ApplicationCall>.()->T
): Deferred<T> = coroutineScope {
    val context: PipelineContext<Unit, ApplicationCall> = withTestApplication() {
        TestActionPipelineContext(
            call(),
            application.coroutineContext
        )
    }
    async {
        with(context) {
            action()
        }
    }
}