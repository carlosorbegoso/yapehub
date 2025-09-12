package org.sysarp.project.service

import android.content.Context
import androidx.work.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.sysarp.project.repository.YapeTransactionRepository
import java.util.concurrent.TimeUnit

/**
 * Manager de trabajo en background para YapeHub
 * Usa AndroidX Work para procesamiento confiable
 */
class BackgroundWorkManager(private val context: Context) {
    
    private val workManager = WorkManager.getInstance(context)
    
    /**
     * Programa trabajo de sincronización periódica
     */
    fun schedulePeriodicSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .setRequiresBatteryNotLow(true)
            .build()
        
        val syncWork = PeriodicWorkRequestBuilder<SyncWorker>(
            repeatInterval = 15, // Cada 15 minutos
            repeatIntervalTimeUnit = TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .addTag("yapehub_sync")
            .build()
        
        workManager.enqueueUniquePeriodicWork(
            "yapehub_periodic_sync",
            ExistingPeriodicWorkPolicy.KEEP,
            syncWork
        )
        
        TimberLogger.i("🔄 Trabajo de sincronización programado")
    }
    
    /**
     * Programa trabajo de limpieza de datos
     */
    fun scheduleDataCleanup() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .build()
        
        val cleanupWork = OneTimeWorkRequestBuilder<CleanupWorker>()
            .setConstraints(constraints)
            .addTag("yapehub_cleanup")
            .build()
        
        workManager.enqueue(cleanupWork)
        TimberLogger.i("🧹 Trabajo de limpieza programado")
    }
    
    /**
     * Cancela todos los trabajos
     */
    fun cancelAllWork() {
        workManager.cancelAllWork()
        TimberLogger.i("❌ Todos los trabajos cancelados")
    }
    
    /**
     * Obtiene el estado de los trabajos
     */
    fun getWorkStatus(): WorkInfo.State? {
        return workManager.getWorkInfosByTag("yapehub_sync")
            .get()
            .firstOrNull()
            ?.state
    }
}

/**
 * Worker para sincronización de datos
 */
class SyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            TimberLogger.i("🔄 Iniciando sincronización de datos")
            
            // Aquí iría la lógica de sincronización
            // Por ejemplo: verificar nuevas transacciones, limpiar datos antiguos, etc.
            
            TimberLogger.i("✅ Sincronización completada")
            Result.success()
        } catch (e: Exception) {
            TimberLogger.error(e, "❌ Error en sincronización")
            Result.retry()
        }
    }
}

/**
 * Worker para limpieza de datos
 */
class CleanupWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            TimberLogger.i("🧹 Iniciando limpieza de datos")
            
            // Aquí iría la lógica de limpieza
            // Por ejemplo: eliminar transacciones muy antiguas, limpiar logs, etc.
            
            TimberLogger.i("✅ Limpieza completada")
            Result.success()
        } catch (e: Exception) {
            TimberLogger.error(e, "❌ Error en limpieza")
            Result.failure()
        }
    }
}
