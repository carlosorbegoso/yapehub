package org.sysarp.project.service

import io.ktor.client.engine.*
import io.ktor.client.engine.darwin.*

actual fun createHttpEngine(): HttpClientEngineFactory<HttpClientEngineConfig> {
    return Darwin.create()
}
