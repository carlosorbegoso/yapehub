package org.sysarp.project.service

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.cio.CIO

internal actual fun getHttpClientEngine(): HttpClientEngine {
    return CIO.create()
}
