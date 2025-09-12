package org.sysarp.project.service

import timber.log.Timber

object TimberLogger {
    fun initialize() {
        // Inicializar Timber para logging
        if (Timber.treeCount == 0) {
            Timber.plant(Timber.DebugTree())
        }
    }
}