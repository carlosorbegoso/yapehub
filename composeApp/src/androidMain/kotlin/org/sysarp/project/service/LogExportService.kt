package org.sysarp.project.service

import android.content.Context
import android.content.Intent

object LogExportService {

    fun shareLogsAsFile(context: Context, logsText: String, fileName: String = "yapehub_logs.txt") {
        try {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, logsText)
                putExtra(Intent.EXTRA_SUBJECT, fileName)
            }
            
            context.startActivity(Intent.createChooser(intent, "Compartir $fileName"))
            
        } catch (e: Exception) {
            // Error compartiendo archivo
        }
    }
}
