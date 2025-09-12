package org.sysarp.project.repository

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import org.sysarp.project.data.BusinessReport
import org.sysarp.project.data.DailyReport
import org.sysarp.project.data.YapeTransaction
import org.sysarp.project.data.TransactionType
import org.sysarp.project.service.DebugLogger
import org.sysarp.project.service.TimberLogger
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.Clock
import java.util.concurrent.ConcurrentHashMap

class YapeTransactionRepositoryImpl(private val context: Context? = null) : YapeTransactionRepository {
    
    private val _transactions = MutableStateFlow<List<YapeTransaction>>(emptyList())
    private val processedSecurityCodes = ConcurrentHashMap<String, Long>()
    
    init {
        TimberLogger.database("🔧 Inicializando repositorio - Contexto: ${context != null}")
        // Cargar transacciones existentes al inicializar si hay contexto
        if (context != null) {
            TimberLogger.database("📂 Cargando transacciones desde base de datos...")
            loadTransactionsFromDatabase()
        } else {
            TimberLogger.w("⚠️ Sin contexto - solo funcionará en memoria")
        }
    }
    
    override suspend fun insertTransaction(transaction: YapeTransaction) {
        val securityCode = transaction.securityCode
        TimberLogger.transaction("💾 Insertando transacción: ${transaction.amount} PEN de ${transaction.senderName}")
        
        if (securityCode != null && processedSecurityCodes.containsKey(securityCode)) {
            TimberLogger.w("⚠️ Transacción duplicada - código ya procesado: $securityCode")
            return
        }

        // Guardar en SQLite si hay contexto
        if (context != null) {
            TimberLogger.database("💾 Guardando en SQLite...")
            saveTransactionToDatabase(transaction)
        } else {
            TimberLogger.w("⚠️ No hay contexto, guardando solo en memoria")
        }
        
        // Actualizar lista en memoria
        val currentList = _transactions.value.toMutableList()
        currentList.add(transaction)
        _transactions.value = currentList
        TimberLogger.database("📊 Total de transacciones en memoria: ${_transactions.value.size}")
        TimberLogger.database("🔄 Emitiendo cambio en StateFlow - Transacciones: ${_transactions.value.size}")

        if (securityCode != null) {
            processedSecurityCodes[securityCode] = Clock.System.now().toEpochMilliseconds()
            TimberLogger.transaction("🔐 Código de seguridad registrado: $securityCode")
        }

        cleanupOldSecurityCodes()
    }
    
    override fun getAllTransactions(): Flow<List<YapeTransaction>> {
        return _transactions.asStateFlow()
    }
    
    override fun getTransactionsByBusiness(businessName: String): Flow<List<YapeTransaction>> {
        return _transactions.asStateFlow().map { transactions ->
            transactions.filter { it.businessName == businessName }
        }
    }
    
    override fun getTransactionsByDateRange(startDate: Instant, endDate: Instant): Flow<List<YapeTransaction>> {
        return _transactions.asStateFlow().map { transactions ->
            transactions.filter {
                it.createdAt >= startDate && it.createdAt <= endDate
            }
        }
    }
    
    override fun getUnprocessedTransactions(): Flow<List<YapeTransaction>> {
        return _transactions.asStateFlow().map { transactions ->
            transactions.filter { !it.isProcessed }
        }
    }
    
    override suspend fun updateTransactionProcessed(transactionId: Long) {
        val currentList = _transactions.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == transactionId }
        if (index != -1) {
            currentList[index] = currentList[index].copy(isProcessed = true)
            _transactions.value = currentList
        }
    }
    
    override suspend fun updateTransactionBusiness(transactionId: Long, businessName: String) {
        val currentList = _transactions.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == transactionId }
        if (index != -1) {
            currentList[index] = currentList[index].copy(businessName = businessName)
            _transactions.value = currentList
        }
    }
    
    override fun getBusinessReports(): Flow<List<BusinessReport>> {
        return _transactions.asStateFlow().map { emptyList() }
    }
    
    override fun getDailyReports(): Flow<List<DailyReport>> {
        return _transactions.asStateFlow().map { emptyList() }
    }
    
    override suspend fun deleteTransaction(transactionId: Long) {
        val currentList = _transactions.value.toMutableList()
        currentList.removeAll { it.id == transactionId }
        _transactions.value = currentList
    }
    
    /**
     * Limpia códigos de seguridad antiguos para evitar memory leaks
     * Se ejecuta automáticamente cuando se agrega una nueva transacción
     */
    private fun cleanupOldSecurityCodes() {
        // Limpiar códigos antiguos si hay más de 1000 (evitar memory leaks)
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
    
    /**
     * Obtiene estadísticas de códigos de seguridad procesados
     */
    fun getSecurityCodeStats(): String {
        return "Códigos procesados: ${processedSecurityCodes.size}, Transacciones: ${_transactions.value.size}"
    }
    
    /**
     * Exporta todas las transacciones a texto plano
     */
    fun exportTransactionsToText(): String {
        val transactions = _transactions.value
        val timestamp = kotlinx.datetime.Clock.System.now()
        val dateFormatter = kotlinx.datetime.TimeZone.currentSystemDefault()
        
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
    
    // Funciones de SQLite para persistencia real
    private fun loadTransactionsFromDatabase() {
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
            
            // Cargar transacciones existentes
            val cursor = db.rawQuery("SELECT * FROM yape_transaction ORDER BY created_at DESC", null)
            val transactions = mutableListOf<YapeTransaction>()
            
            while (cursor.moveToNext()) {
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
                    processedAt = cursor.getLong(cursor.getColumnIndexOrThrow("processed_at")).let { if (it == 0L) null else Instant.fromEpochMilliseconds(it) },
                    isProcessed = cursor.getInt(cursor.getColumnIndexOrThrow("is_processed")) == 1,
                    rawNotification = cursor.getString(cursor.getColumnIndexOrThrow("raw_notification")),
                    securityCode = cursor.getString(cursor.getColumnIndexOrThrow("security_code"))
                )
                transactions.add(transaction)
                
                // Cargar códigos de seguridad procesados
                if (transaction.securityCode != null) {
                    processedSecurityCodes[transaction.securityCode] = transaction.createdAt.toEpochMilliseconds()
                }
            }
            
            cursor.close()
            db.close()
            
            _transactions.value = transactions
            DebugLogger.info("Cargadas ${transactions.size} transacciones desde SQLite")
            
        } catch (e: Exception) {
            DebugLogger.error("Error cargando transacciones desde SQLite: ${e.message}")
        }
    }
    
    private fun saveTransactionToDatabase(transaction: YapeTransaction) {
        try {
            val context = this.context ?: run {
                TimberLogger.error("❌ No hay contexto para guardar en base de datos")
                return
            }
            TimberLogger.database("💾 Abriendo base de datos SQLite...")
            val db = context.openOrCreateDatabase("yape_transactions.db", Context.MODE_PRIVATE, null)
            
            db.execSQL("""
                INSERT OR REPLACE INTO yape_transaction 
                (transaction_id, amount, currency, sender_name, sender_phone, message, 
                 transaction_type, business_name, created_at, processed_at, is_processed, 
                 raw_notification, security_code)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """, arrayOf(
                transaction.transactionId,
                transaction.amount,
                transaction.currency,
                transaction.senderName,
                transaction.senderPhone,
                transaction.message,
                transaction.transactionType.name,
                transaction.businessName,
                transaction.createdAt.toEpochMilliseconds(),
                transaction.processedAt?.toEpochMilliseconds() ?: 0L,
                if (transaction.isProcessed) 1 else 0,
                transaction.rawNotification,
                transaction.securityCode
            ))
            
            db.close()
            TimberLogger.database("✅ Transacción guardada exitosamente en SQLite: ${transaction.transactionId}")
            
            // CRÍTICO: Recargar transacciones desde SQLite y actualizar StateFlow
            loadTransactionsFromDatabase()
            
        } catch (e: Exception) {
            TimberLogger.error(e, "❌ Error guardando transacción en SQLite: ${e.message}")
        }
    }
}