package org.sysarp.project.service

import io.ktor.client.engine.HttpClientEngineConfig
import io.ktor.client.engine.HttpClientEngineFactory

actual fun createHttpEngine(): HttpClientEngineFactory<HttpClientEngineConfig> {
    // Usar el engine por defecto para iOS
    return HttpClientEngineFactory()
}
