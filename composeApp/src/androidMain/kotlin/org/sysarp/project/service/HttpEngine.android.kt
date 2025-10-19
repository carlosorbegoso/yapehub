package org.sysarp.project.service

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp

internal actual fun getHttpClientEngine(): HttpClientEngine {
    return OkHttp.create {
        // Configuración específica para Android con soporte completo para WebSockets
        // OkHttp engine soporta WebSockets nativamente
    }
}