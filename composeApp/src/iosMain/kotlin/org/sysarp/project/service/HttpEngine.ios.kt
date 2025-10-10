package org.sysarp.project.service

import io.ktor.client.engine.*

actual fun createHttpEngine(): HttpClientEngineFactory<HttpClientEngineConfig> {
    // Usar el engine por defecto para iOS
    return HttpClientEngineFactory()
}
