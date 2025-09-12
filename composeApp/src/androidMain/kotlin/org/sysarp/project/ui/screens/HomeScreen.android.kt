package org.sysarp.project.ui.screens

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.sysarp.project.ContextProvider
import org.sysarp.project.service.LogExportService

/**
 * Implementación específica de Android para exportar logs
 */
actual fun exportLogs(logsText: String) {
    val context = ContextProvider.getContext()
    if (context != null) {
        CoroutineScope(Dispatchers.Main).launch {
            LogExportService.shareLogsAsFile(context, logsText)
        }
    }
}

/**
 * Implementación específica de Android para exportar base de datos
 */
actual fun exportDatabase(databaseText: String, fileName: String) {
    val context = ContextProvider.getContext()
    if (context != null) {
        CoroutineScope(Dispatchers.Main).launch {
            LogExportService.shareLogsAsFile(context, databaseText, fileName)
        }
    }
}
