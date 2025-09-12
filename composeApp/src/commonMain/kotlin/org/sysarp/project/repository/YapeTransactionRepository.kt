package org.sysarp.project.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant
import org.sysarp.project.data.BusinessReport
import org.sysarp.project.data.DailyReport
import org.sysarp.project.data.TransactionType
import org.sysarp.project.data.YapeTransaction

interface YapeTransactionRepository {
    
    fun getAllTransactions(): Flow<List<YapeTransaction>>
    
    fun getTransactionsByBusiness(businessName: String): Flow<List<YapeTransaction>>
    
    fun getTransactionsByDateRange(startDate: Instant, endDate: Instant): Flow<List<YapeTransaction>>
    
    fun getUnprocessedTransactions(): Flow<List<YapeTransaction>>
    
    suspend fun insertTransaction(transaction: YapeTransaction)
    
    suspend fun updateTransactionProcessed(transactionId: Long)
    
    suspend fun updateTransactionBusiness(transactionId: Long, businessName: String)
    
    suspend fun deleteTransaction(transactionId: Long)
    
    fun getBusinessReports(): Flow<List<BusinessReport>>
    
    fun getDailyReports(): Flow<List<DailyReport>>

    // Funciones de exportación
    fun exportTransactionsToText(): String

    fun exportAllTransactionsToText(): String
}