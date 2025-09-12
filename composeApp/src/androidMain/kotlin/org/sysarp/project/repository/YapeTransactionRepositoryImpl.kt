package org.sysarp.project.repository

import android.content.Context
import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import org.sysarp.project.data.BusinessReport
import org.sysarp.project.data.DailyReport
import org.sysarp.project.data.YapeTransaction
import org.sysarp.project.data.TransactionType
import org.sysarp.project.service.DebugLogger
import org.sysarp.project.service.TimberLogger
import kotlinx.datetime.Instant
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.util.concurrent.ConcurrentHashMap

class YapeTransactionRepositoryImpl(private val context: Context? = null) : YapeTransactionRepository {
    
    // Solo para notificar cambios a los observadores
    private val _dataChangeTrigger = MutableStateFlow(0L)
    private val processedSecurityCodes = ConcurrentHashMap<String, Long>()
    
    init {
        TimberLogger.database("🔧 Inicializando repositorio - Contexto: ${context != null}")
        if (context != null) {
            initializeDatabase()
            loadProcessedSecurityCodes()
        } else {
            TimberLogger.w("⚠️ Sin contexto - funcionalidad limitada")
        }
    }
    
    override suspend fun insertTransaction(transaction: YapeTransaction) {
        val securityCode = transaction.securityCode
        TimberLogger.transaction("💾 [INICIO] Insertando transacción: ${transaction.amount} PEN de ${transaction.senderName}")
        TimberLogger.transaction("🔍 [DETALLES] ID: ${transaction.transactionId}, Código: $securityCode, Tipo: ${transaction.transactionType}")

        // Verificar si es duplicado para logging, pero SIEMPRE guardar
        val isDuplicate = securityCode != null && isDuplicateTransaction(transaction)
        if (isDuplicate) {
            TimberLogger.w("⚠️ Transacción duplicada detectada: $securityCode - ${transaction.amount} PEN de ${transaction.senderName} - PERO SE GUARDA IGUAL")
        }

        if (context == null) {
            TimberLogger.error("❌ [CRÍTICO] No hay contexto para guardar en base de datos")
            return
        }

        // Generar un transactionId único y consistente
        val uniqueTransactionId = generateUniqueTransactionId(transaction)
        val transactionWithUniqueId = transaction.copy(transactionId = uniqueTransactionId)

        TimberLogger.transaction("🔧 [ID GENERADO] TransactionId único: $uniqueTransactionId")

        // SIEMPRE guardar en SQLite (incluso duplicados)
        val success = saveTransactionToDatabase(transactionWithUniqueId)
        if (success) {
            TimberLogger.database("✅ [ÉXITO] Transacción guardada exitosamente en SQLite ${if (isDuplicate) "(DUPLICADO PRESERVADO)" else "(NUEVA)"}")
            TimberLogger.transaction("💾 [CONFIRMACIÓN] Base de datos actualizada correctamente")

            if (securityCode != null) {
                processedSecurityCodes[securityCode] = Clock.System.now().toEpochMilliseconds()
                TimberLogger.transaction("🔐 [CACHÉ] Código de seguridad registrado: $securityCode")
            }

            // Notificar cambios para que los observadores se actualicen
            _dataChangeTrigger.value = Clock.System.now().toEpochMilliseconds()
            TimberLogger.transaction("🔄 [NOTIFICACIÓN] Observadores notificados de cambios")
            cleanupOldSecurityCodes()
            
            // Verificación adicional: contar transacciones en BD
            val totalTransactions = getTransactionsFromDatabase().size
            TimberLogger.database("📊 [VERIFICACIÓN] Total transacciones en BD: $totalTransactions")
        } else {
            TimberLogger.error("❌ [ERROR CRÍTICO] Falló el guardado en SQLite - revisando configuración")
            TimberLogger.error("🔧 [DEBUG] Context: ${context != null}, Transaction: ${transaction.transactionId}")
        }
    }

    private fun generateUniqueTransactionId(transaction: YapeTransaction): String {
        val baseString = "${transaction.securityCode}_${transaction.amount}_${transaction.senderName?.take(5) ?: "unknown"}"
        val hash = baseString.hashCode().toString().replace("-", "")
        return "YAPE_${hash}_${transaction.securityCode ?: "NOCODE"}"
    }

    override fun getAllTransactions(): Flow<List<YapeTransaction>> {
        return _dataChangeTrigger.asStateFlow().map {
            getUniqueTransactionsFromDatabase()
        }
    }
    
    /**
     * Obtiene todas las transacciones (incluyendo duplicados) para análisis completo
     */
    fun getAllTransactionsIncludingDuplicates(): Flow<List<YapeTransaction>> {
        return _dataChangeTrigger.asStateFlow().map {
            getTransactionsFromDatabase()
        }
    }
    
    override fun getTransactionsByBusiness(businessName: String): Flow<List<YapeTransaction>> {
        return _dataChangeTrigger.asStateFlow().map {
            getTransactionsFromDatabase().filter { it.businessName == businessName }
        }
    }
    
    override fun getTransactionsByDateRange(startDate: Instant, endDate: Instant): Flow<List<YapeTransaction>> {
        return _dataChangeTrigger.asStateFlow().map {
            getTransactionsFromDatabase().filter {
                it.createdAt >= startDate && it.createdAt <= endDate
            }
        }
    }
    
    override fun getUnprocessedTransactions(): Flow<List<YapeTransaction>> {
        return _dataChangeTrigger.asStateFlow().map {
            getTransactionsFromDatabase().filter { !it.isProcessed }
        }
    }
    
    override suspend fun updateTransactionProcessed(transactionId: Long) {
        val context = this.context ?: return
        try {
            val db = context.openOrCreateDatabase("yape_transactions.db", Context.MODE_PRIVATE, null)
            val values = ContentValues().apply {
                put("is_processed", 1)
                put("processed_at", Clock.System.now().toEpochMilliseconds())
            }
            db.update("yape_transaction", values, "id = ?", arrayOf(transactionId.toString()))
            db.close()

            // Notificar cambios
            _dataChangeTrigger.value = Clock.System.now().toEpochMilliseconds()
        } catch (e: Exception) {
            TimberLogger.error("❌ Error actualizando transacción: ${e.message}")
        }
    }
    
    override suspend fun updateTransactionBusiness(transactionId: Long, businessName: String) {
        val context = this.context ?: return
        try {
            val db = context.openOrCreateDatabase("yape_transactions.db", Context.MODE_PRIVATE, null)
            val values = ContentValues().apply {
                put("business_name", businessName)
            }
            db.update("yape_transaction", values, "id = ?", arrayOf(transactionId.toString()))
            db.close()

            // Notificar cambios
            _dataChangeTrigger.value = Clock.System.now().toEpochMilliseconds()
        } catch (e: Exception) {
            TimberLogger.error("❌ Error actualizando negocio: ${e.message}")
        }
    }
    
    override fun getBusinessReports(): Flow<List<BusinessReport>> {
        return _dataChangeTrigger.asStateFlow().map { emptyList() }
    }
    
    override fun getDailyReports(): Flow<List<DailyReport>> {
        return _dataChangeTrigger.asStateFlow().map { emptyList() }
    }
    
    override suspend fun deleteTransaction(transactionId: Long) {
        val context = this.context ?: return
        try {
            val db = context.openOrCreateDatabase("yape_transactions.db", Context.MODE_PRIVATE, null)
            db.delete("yape_transaction", "id = ?", arrayOf(transactionId.toString()))
            db.close()

            // Notificar cambios
            _dataChangeTrigger.value = Clock.System.now().toEpochMilliseconds()
        } catch (e: Exception) {
            TimberLogger.error("❌ Error eliminando transacción: ${e.message}")
        }
    }
    
    /**
     * Obtiene transacciones únicas (sin duplicados) para mostrar en la UI
     */
    private fun getUniqueTransactionsFromDatabase(): List<YapeTransaction> {
        val allTransactions = getTransactionsFromDatabase()
        val uniqueTransactions = mutableListOf<YapeTransaction>()
        val seenSecurityCodes = mutableSetOf<String>()
        
        TimberLogger.database("🔍 [UNIQUE FILTER] Procesando ${allTransactions.size} transacciones totales")
        
        // Ordenar por fecha de creación (más recientes primero)
        val sortedTransactions = allTransactions.sortedByDescending { it.createdAt }
        
        for (transaction in sortedTransactions) {
            val securityCode = transaction.securityCode
            TimberLogger.database("🔍 [UNIQUE FILTER] Procesando: ${transaction.senderName} - ${transaction.amount} PEN - código: $securityCode")
            
            if (securityCode == null || !seenSecurityCodes.contains(securityCode)) {
                uniqueTransactions.add(transaction)
                TimberLogger.database("✅ [UNIQUE FILTER] Agregada: ${transaction.senderName} - ${transaction.amount} PEN")
                if (securityCode != null) {
                    seenSecurityCodes.add(securityCode)
                }
            } else {
                TimberLogger.database("❌ [UNIQUE FILTER] Duplicada (filtrada): ${transaction.senderName} - ${transaction.amount} PEN - código: $securityCode")
            }
        }
        
        TimberLogger.database("📊 Transacciones únicas: ${uniqueTransactions.size} de ${allTransactions.size} total")
        return uniqueTransactions
    }
    
    /**
     * Obtiene todas las transacciones directamente desde SQLite (incluyendo duplicados)
     */
    private fun getTransactionsFromDatabase(): List<YapeTransaction> {
        val context = this.context ?: return emptyList()

        try {
            val db = context.openOrCreateDatabase("yape_transactions.db", Context.MODE_PRIVATE, null)
            val cursor = db.rawQuery("SELECT * FROM yape_transaction ORDER BY created_at DESC", null)
            val transactions = mutableListOf<YapeTransaction>()

            while (cursor.moveToNext()) {
                try {
                    val transaction = YapeTransaction(
                        id = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                        transactionId = cursor.getString(cursor.getColumnIndexOrThrow("transaction_id")),
                        amount = cursor.getDouble(cursor.getColumnIndexOrThrow("amount")),
                        currency = cursor.getString(cursor.getColumnIndexOrThrow("currency")),
                        senderName = cursor.getString(cursor.getColumnIndexOrThrow("sender_name")) ?: "",
                        senderPhone = cursor.getString(cursor.getColumnIndexOrThrow("sender_phone")),
                        message = cursor.getString(cursor.getColumnIndexOrThrow("message")),
                        transactionType = TransactionType.valueOf(cursor.getString(cursor.getColumnIndexOrThrow("transaction_type"))),
                        businessName = cursor.getString(cursor.getColumnIndexOrThrow("business_name")),
                        createdAt = Instant.fromEpochMilliseconds(cursor.getLong(cursor.getColumnIndexOrThrow("created_at"))),
                        processedAt = cursor.getLong(cursor.getColumnIndexOrThrow("processed_at")).let {
                            if (it == 0L) null else Instant.fromEpochMilliseconds(it)
                        },
                        isProcessed = cursor.getInt(cursor.getColumnIndexOrThrow("is_processed")) == 1,
                        rawNotification = cursor.getString(cursor.getColumnIndexOrThrow("raw_notification")),
                        securityCode = cursor.getString(cursor.getColumnIndexOrThrow("security_code"))
                    )
                    transactions.add(transaction)
                } catch (e: Exception) {
                    TimberLogger.error("❌ Error mapeando transacción: ${e.message}")
                }
            }

            cursor.close()
            db.close()

            TimberLogger.database("📊 Cargadas ${transactions.size} transacciones desde SQLite")
            return transactions

        } catch (e: Exception) {
            TimberLogger.error("❌ Error leyendo transacciones: ${e.message}")
            return emptyList()
        }
    }

    private fun initializeDatabase() {
        try {
            val context = this.context ?: return
            val db = context.openOrCreateDatabase("yape_transactions.db", Context.MODE_PRIVATE, null)

            // Crear tabla si no existe
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS yape_transaction (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    transaction_id TEXT UNIQUE NOT NULL,
                    amount REAL NOT NULL,
                    currency TEXT NOT NULL DEFAULT 'PEN',
                    sender_name TEXT,
                    sender_phone TEXT,
                    message TEXT,
                    transaction_type TEXT NOT NULL,
                    business_name TEXT,
                    created_at INTEGER NOT NULL,
                    processed_at INTEGER,
                    is_processed INTEGER NOT NULL DEFAULT 0,
                    raw_notification TEXT,
                    security_code TEXT
                )
            """)

            // ELIMINAR el índice único en security_code - permitir duplicados
            // db.execSQL("""
            //     CREATE UNIQUE INDEX IF NOT EXISTS idx_security_code
            //     ON yape_transaction(security_code)
            //     WHERE security_code IS NOT NULL
            // """)

            // En su lugar, crear un índice normal (no único) para mejorar performance
            db.execSQL("""
                CREATE INDEX IF NOT EXISTS idx_security_code_normal 
                ON yape_transaction(security_code) 
                WHERE security_code IS NOT NULL
            """)

            db.close()
            TimberLogger.database("✅ Base de datos inicializada correctamente - SIN restricción única en security_code")

        } catch (e: Exception) {
            TimberLogger.error("❌ Error inicializando base de datos: ${e.message}")
        }
    }

    private fun loadProcessedSecurityCodes() {
        try {
            val transactions = getTransactionsFromDatabase()
            transactions.forEach { transaction ->
                if (transaction.securityCode != null) {
                    processedSecurityCodes[transaction.securityCode] = transaction.createdAt.toEpochMilliseconds()
                }
            }
            TimberLogger.database("🔐 Cargados ${processedSecurityCodes.size} códigos de seguridad procesados")
        } catch (e: Exception) {
            TimberLogger.error("❌ Error cargando códigos de seguridad: ${e.message}")
        }
    }

    private fun cleanupOldSecurityCodes() {
        if (processedSecurityCodes.size > 1000) {
            val removedCount = processedSecurityCodes.size - 500
            val codesList = processedSecurityCodes.keys.toList()
            val codesToKeep = codesList.takeLast(500).toSet()
            processedSecurityCodes.clear()
            codesToKeep.forEach { code ->
                processedSecurityCodes[code] = Clock.System.now().toEpochMilliseconds()
            }
            DebugLogger.info("🧹 Limpiados $removedCount códigos de seguridad antiguos")
        }
    }
    
    fun exportTransactionsToText(): String {
        val transactions = getTransactionsFromDatabase()
        val timestamp = Clock.System.now()
        val dateFormatter = TimeZone.currentSystemDefault()

        val header = """
========================================
YAPEHUB - EXPORTACIÓN DE BASE DE DATOS
========================================
Exportado: ${timestamp.toLocalDateTime(dateFormatter)}
Total de transacciones: ${transactions.size}
========================================

"""
        
        val transactionsText = if (transactions.isEmpty()) {
            "No hay transacciones en la base de datos."
        } else {
            transactions.mapIndexed { index, transaction ->
                """
[${index + 1}] ID: ${transaction.id}
    Transaction ID: ${transaction.transactionId}
    Monto: ${transaction.amount} ${transaction.currency}
    Remitente: ${transaction.senderName}
    Teléfono: ${transaction.senderPhone ?: "N/A"}
    Mensaje: ${transaction.message ?: "N/A"}
    Tipo: ${transaction.transactionType}
    Negocio: ${transaction.businessName ?: "Sin categorizar"}
    Código de seguridad: ${transaction.securityCode ?: "N/A"}
    Creado: ${transaction.createdAt.toLocalDateTime(dateFormatter)}
    Procesado: ${if (transaction.isProcessed) "Sí" else "No"}
    Procesado en: ${transaction.processedAt?.toLocalDateTime(dateFormatter) ?: "N/A"}
    Notificación original: ${transaction.rawNotification ?: "N/A"}
    ----------------------------------------
""".trimIndent()
            }.joinToString("\n")
        }
        
        return header + transactionsText
    }
    
    private fun saveTransactionToDatabase(transaction: YapeTransaction): Boolean {
        val context = this.context ?: return false

        return try {
            val db = context.openOrCreateDatabase("yape_transactions.db", Context.MODE_PRIVATE, null)
            
            val values = ContentValues().apply {
                put("transaction_id", transaction.transactionId)
                put("amount", transaction.amount)
                put("currency", transaction.currency)
                put("sender_name", transaction.senderName)
                put("sender_phone", transaction.senderPhone)
                put("message", transaction.message)
                put("transaction_type", transaction.transactionType.name)
                put("business_name", transaction.businessName)
                put("created_at", transaction.createdAt.toEpochMilliseconds())
                put("processed_at", transaction.processedAt?.toEpochMilliseconds() ?: 0L)
                put("is_processed", if (transaction.isProcessed) 1 else 0)
                put("raw_notification", transaction.rawNotification)
                put("security_code", transaction.securityCode)
            }

            TimberLogger.database("💾 [SAVE] Guardando: ${transaction.transactionId} - ${transaction.amount} PEN")
            TimberLogger.database("🔍 [SAVE] Datos: sender=${transaction.senderName}, code=${transaction.securityCode}")

            // Usar REPLACE para manejar conflictos de transaction_id único
            val result = db.insertWithOnConflict("yape_transaction", null, values, SQLiteDatabase.CONFLICT_REPLACE)

            // Verificar que la inserción fue exitosa
            if (result != -1L) {
                TimberLogger.database("✅ [SAVE] Transacción guardada con ID SQLite: $result")

                // Verificación adicional: leer la transacción recién guardada
                val cursor = db.rawQuery("SELECT COUNT(*) FROM yape_transaction WHERE transaction_id = ?", arrayOf(transaction.transactionId))
                cursor.moveToFirst()
                val count = cursor.getInt(0)
                cursor.close()

                if (count > 0) {
                    TimberLogger.database("✅ [SAVE] Verificación exitosa: transacción existe en BD")
                } else {
                    TimberLogger.error("❌ [SAVE] ERROR: transacción no encontrada después de insertar")
                }

                db.close()
                true
            } else {
                TimberLogger.error("❌ [SAVE] Error: insertWithOnConflict retornó -1")
                db.close()
                false
            }

        } catch (e: Exception) {
            TimberLogger.error("❌ [SAVE] Error crítico guardando en SQLite: ${e.message}")
            TimberLogger.error("🔧 [SAVE] Stack trace: ${e.stackTraceToString()}")
            false
        }
    }

    /**
     * Verifica si una transacción es duplicada considerando múltiples factores
     */
    private fun isDuplicateTransaction(transaction: YapeTransaction): Boolean {
        val securityCode = transaction.securityCode ?: return false

        // Si el código no existe en la caché, no es duplicado
        if (!processedSecurityCodes.containsKey(securityCode)) {
            return false
        }

        // Verificar en la base de datos si existe una transacción con el mismo código, monto y remitente
        val existingTransactions = getTransactionsFromDatabase()
        val duplicateFound = existingTransactions.any { existing ->
            existing.securityCode == securityCode &&
            existing.amount == transaction.amount &&
            existing.senderName == transaction.senderName
        }

        if (duplicateFound) {
            TimberLogger.w("🔍 Transacción duplicada confirmada en BD: código=$securityCode, monto=${transaction.amount}, remitente=${transaction.senderName}")
        } else {
            DebugLogger.info("✅ Transacción válida con código existente pero diferentes datos: código=$securityCode")
        }

        return duplicateFound
    }

    /**
     * Inserta una transacción forzando la inserción, ignorando duplicados
     * Útil para transacciones perdidas que no se guardaron correctamente
     */
    fun forceInsertTransaction(transaction: YapeTransaction) {
        TimberLogger.transaction("🔧 FORZANDO inserción de transacción: ${transaction.amount} PEN de ${transaction.senderName}")

        if (context == null) {
            TimberLogger.error("❌ No hay contexto para guardar en base de datos")
            return
        }

        // Generar un transactionId único y consistente
        val uniqueTransactionId = generateUniqueTransactionId(transaction)
        val transactionWithUniqueId = transaction.copy(transactionId = uniqueTransactionId)

        // Guardar SOLO en SQLite, ignorando duplicados
        val success = saveTransactionToDatabase(transactionWithUniqueId)
        if (success) {
            TimberLogger.database("✅ Transacción FORZADA guardada exitosamente")

            val securityCode = transaction.securityCode
            if (securityCode != null) {
                processedSecurityCodes[securityCode] = Clock.System.now().toEpochMilliseconds()
                TimberLogger.transaction("🔐 Código de seguridad registrado: $securityCode")
            }

            // Notificar cambios para que los observadores se actualicen
            _dataChangeTrigger.value = Clock.System.now().toEpochMilliseconds()
            cleanupOldSecurityCodes()
        } else {
            TimberLogger.error("❌ Error guardando transacción forzada")
        }
    }

    /**
     * Método de utilidad para insertar manualmente la transacción perdida de Carlos Orbegoso
     */
    suspend fun insertMissingCarlosTransaction() {
        val missingTransaction = YapeTransaction(
            id = 0L,
            transactionId = "",
            amount = 0.1,
            currency = "PEN",
            senderName = "Carlos Orbegoso L.",
            senderPhone = null,
            message = "Confirmación de Pago Carlos Orbegoso L. te envió un pago por S/ 0.1. El cód. de seguridad es: 962",
            transactionType = TransactionType.RECEIVED,
            businessName = null,
            createdAt = Clock.System.now(),
            processedAt = null,
            isProcessed = false,
            rawNotification = "Confirmación de Pago Carlos Orbegoso L. te envió un pago por S/ 0.1. El cód. de seguridad es: 962",
            securityCode = "962"
        )

        DebugLogger.info("🔧 Insertando transacción perdida de Carlos Orbegoso L. por 0.1 PEN")
        forceInsertTransaction(missingTransaction)
    }

    /**
     * Limpia la transacción de prueba y agrega la transacción perdida de Carlos
     */
    suspend fun cleanupAndRecoverTransactions() {
        DebugLogger.info("🧹 Iniciando limpieza y recuperación de transacciones...")

        // 1. Eliminar la transacción de prueba
        val testTransactions = getTransactionsFromDatabase().filter {
            it.senderName == "Usuario de Prueba" && it.amount == 25.5 && it.securityCode == "123"
        }

        testTransactions.forEach { testTransaction ->
            DebugLogger.info("🗑️ Eliminando transacción de prueba: ${testTransaction.transactionId}")
            deleteTransaction(testTransaction.id)
        }

        // 2. Verificar si ya existe la transacción de Carlos
        val carlosExists = getTransactionsFromDatabase().any {
            it.senderName == "Carlos Orbegoso L." && it.amount == 0.1 && it.securityCode == "962"
        }

        // 3. Si no existe, insertarla
        if (!carlosExists) {
            DebugLogger.info("✅ Insertando transacción perdida de Carlos Orbegoso L.")
            insertMissingCarlosTransaction()
        } else {
            DebugLogger.info("ℹ️ La transacción de Carlos Orbegoso L. ya existe")
        }

        DebugLogger.info("✅ Limpieza y recuperación completada")
    }
}

