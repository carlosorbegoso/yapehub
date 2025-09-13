package org.sysarp.project.service

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin

internal actual fun getHttpClientEngine(): HttpClientEngine {
    return Darwin.create()
}

