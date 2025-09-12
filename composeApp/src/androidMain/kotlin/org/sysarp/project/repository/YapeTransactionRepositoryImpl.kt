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

class YapeTransactionRepositoryImpl(private val context: Context? = null) : YapeTransactionRepository {
    
    // Solo para notificar cambios a los observadores
    private val _dataChangeTrigger = MutableStateFlow(0L)
    
    init {
        TimberLogger.database("🔧 Inicializando repositorio - Contexto: ${context != null}")
        if (context != null) {
            initializeDatabase()
        } else {
            TimberLogger.w("⚠️ Sin contexto - funcionalidad limitada")
        }
    }
    
    override suspend fun insertTransaction(transaction: YapeTransaction) {
        val securityCode = transaction.securityCode
        TimberLogger.transaction("💾 [INICIO] Insertando transacción: ${transaction.amount} PEN de ${transaction.senderName}")
        TimberLogger.transaction("🔍 [DETALLES] Código: $securityCode, Tipo: ${transaction.transactionType}")

        if (context == null) {
            TimberLogger.error("❌ [CRÍTICO] No hay contexto para guardar en base de datos")
            return
        }

        // Generar un transactionId único usando timestamp + código de seguridad
        val uniqueTransactionId = generateUniqueTransactionId(transaction)
        val transactionWithUniqueId = transaction.copy(transactionId = uniqueTransactionId)

        TimberLogger.transaction("🔧 [ID GENERADO] TransactionId único: $uniqueTransactionId")

        // Guardar en SQLite
        val success = saveTransactionToDatabase(transactionWithUniqueId)
        if (success) {
            TimberLogger.database("✅ [ÉXITO] Transacción guardada exitosamente en SQLite")
            TimberLogger.transaction("💾 [CONFIRMACIÓN] Base de datos actualizada correctamente")

            // Notificar cambios para que los observadores se actualicen
            _dataChangeTrigger.value = Clock.System.now().toEpochMilliseconds()
            TimberLogger.transaction("🔄 [NOTIFICACIÓN] Observadores notificados de cambios")

            // Verificación adicional: contar transacciones en BD
            val totalTransactions = getTransactionsFromDatabase().size
            TimberLogger.database("📊 [VERIFICACIÓN] Total transacciones en BD: $totalTransactions")
        } else {
            TimberLogger.error("❌ [ERROR CRÍTICO] Falló el guardado en SQLite")
        }
    }

    private fun generateUniqueTransactionId(transaction: YapeTransaction): String {
        // MEJORADO: Usar timestamp + código de seguridad como clave única
        val timestamp = Clock.System.now().toEpochMilliseconds()
        val securityCode = transaction.securityCode ?: "NOCODE"
        val senderHash = (transaction.senderName ?: "unknown").hashCode().toString().replace("-", "")
        return "YAPE_${timestamp}_${securityCode}_${senderHash}"
    }

    override fun getAllTransactions(): Flow<List<YapeTransaction>> {
        return _dataChangeTrigger.asStateFlow().map {
            getUniqueTransactionsFromDatabase()
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

            _dataChangeTrigger.value = Clock.System.now().toEpochMilliseconds()
        } catch (e: Exception) {
            TimberLogger.error("❌ Error eliminando transacción: ${e.message}")
        }
    }
    
    /**
     * Obtiene transacciones únicas para mostrar en la UI
     * OPTIMIZADO: Mostrar todas las transacciones sin filtro agresivo
     */
    private fun getUniqueTransactionsFromDatabase(): List<YapeTransaction> {
        val allTransactions = getTransactionsFromDatabase()

        TimberLogger.database("🔍 [FILTER] Cargando ${allTransactions.size} transacciones totales")

        // Solo ordenar por fecha (más recientes primero) - sin filtrar duplicados
        val sortedTransactions = allTransactions.sortedByDescending { it.createdAt }
        
        sortedTransactions.forEach { transaction ->
            TimberLogger.database("✅ [MOSTRAR] ${transaction.senderName} - ${transaction.amount} PEN - código: ${transaction.securityCode}")
        }
        
        TimberLogger.database("📊 Transacciones mostradas: ${sortedTransactions.size}")
        return sortedTransactions
    }
    
    /**
     * Obtiene todas las transacciones directamente desde SQLite
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

            // Crear índice para mejorar performance
            db.execSQL("""
                CREATE INDEX IF NOT EXISTS idx_security_code_normal 
                ON yape_transaction(security_code) 
                WHERE security_code IS NOT NULL
            """)

            db.close()
            TimberLogger.database("✅ Base de datos inicializada correctamente")

        } catch (e: Exception) {
            TimberLogger.error("❌ Error inicializando base de datos: ${e.message}")
        }
    }

    override fun exportTransactionsToText(): String {
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
    Código de seguridad: ${transaction.securityCode ?: "N/A"}
    Creado: ${transaction.createdAt.toLocalDateTime(dateFormatter)}
    ----------------------------------------
""".trimIndent()
            }.joinToString("\n")
        }
        
        return header + transactionsText
    }
    
    override fun exportAllTransactionsToText(): String {
        val transactions = getTransactionsFromDatabase()
        val timestamp = Clock.System.now()
        val dateFormatter = TimeZone.currentSystemDefault()

        val header = """
========================================
YAPEHUB - EXPORTACIÓN COMPLETA DE BD
========================================
Exportado: ${timestamp.toLocalDateTime(dateFormatter)}
Total de transacciones: ${transactions.size}
========================================

"""

        val transactionsText = if (transactions.isEmpty()) {
            "No hay transacciones en la base de datos SQLite."
        } else {
            transactions.mapIndexed { index, transaction ->
                """
[${index + 1}] ID SQLite: ${transaction.id}
    Transaction ID: ${transaction.transactionId}
    Monto: ${transaction.amount} ${transaction.currency}
    Remitente: ${transaction.senderName}
    Código de seguridad: ${transaction.securityCode ?: "N/A"}
    Creado: ${transaction.createdAt.toLocalDateTime(dateFormatter)}
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

            // Usar REPLACE para manejar conflictos de transaction_id único
            val result = db.insertWithOnConflict("yape_transaction", null, values, SQLiteDatabase.CONFLICT_REPLACE)

            if (result != -1L) {
                TimberLogger.database("✅ [SAVE] Transacción guardada con ID SQLite: $result")

                // Verificación adicional
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
            false
        }
    }
}
