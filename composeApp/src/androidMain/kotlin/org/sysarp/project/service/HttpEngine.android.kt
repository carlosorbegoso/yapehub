package org.sysarp.project.service

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.android.Android

internal actual fun getHttpClientEngine(): HttpClientEngine {
    return Android.create()
}