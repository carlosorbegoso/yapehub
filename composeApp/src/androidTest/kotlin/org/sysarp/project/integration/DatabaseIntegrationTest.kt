package org.sysarp.project.integration

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.sysarp.project.data.TransactionType
import org.sysarp.project.data.YapeTransaction
import org.sysarp.project.repository.YapeTransactionRepositoryImpl
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
class DatabaseIntegrationTest {
    
    private lateinit var repository: YapeTransactionRepositoryImpl
    private lateinit var context: Context
    
    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        repository = YapeTransactionRepositoryImpl(context)
    }
    
    @After
    fun cleanup() {
        // Limpiar base de datos después de cada test
        context.deleteDatabase("yape_transactions.db")
    }
    
    @Test
    fun `test database persistence across repository instances`() = runBlocking {
        // Given
        val transaction1 = YapeTransaction(
            id = 0,
            transactionId = "PERSIST_001",
            amount = 15.50,
            currency = "PEN",
            senderName = "Persistence Test User",
            transactionType = TransactionType.RECEIVED,
            businessName = "Persistence Business",
            createdAt = Clock.System.now(),
            securityCode = "PERSIST_001"
        )
        
        val transaction2 = YapeTransaction(
            id = 0,
            transactionId = "PERSIST_002",
            amount = 25.75,
            currency = "PEN",
            senderName = "Another User",
            transactionType = TransactionType.SENT,
            businessName = "Another Business",
            createdAt = Clock.System.now(),
            securityCode = "PERSIST_002"
        )
        
        // When - Insert transactions with first repository instance
        repository.insertTransaction(transaction1)
        repository.insertTransaction(transaction2)
        
        // Create new repository instance (simulating app restart)
        val newRepository = YapeTransactionRepositoryImpl(context)
        
        // Then - Verify data persisted
        val allTransactions = newRepository.getAllTransactions().first()
        assertEquals(2, allTransactions.size)
        
        val receivedTransaction = allTransactions.find { it.transactionId == "PERSIST_001" }
        assertNotNull(receivedTransaction)
        assertEquals(15.50, receivedTransaction.amount)
        assertEquals("Persistence Test User", receivedTransaction.senderName)
        assertEquals(TransactionType.RECEIVED, receivedTransaction.transactionType)
        
        val sentTransaction = allTransactions.find { it.transactionId == "PERSIST_002" }
        assertNotNull(sentTransaction)
        assertEquals(25.75, sentTransaction.amount)
        assertEquals("Another User", sentTransaction.senderName)
        assertEquals(TransactionType.SENT, sentTransaction.transactionType)
    }
    
    @Test
    fun `test security code deduplication persistence`() = runBlocking {
        // Given
        val securityCode = "DEDUP_TEST_123"
        val transaction1 = YapeTransaction(
            id = 0,
            transactionId = "DEDUP_001",
            amount = 10.00,
            currency = "PEN",
            senderName = "User 1",
            transactionType = TransactionType.RECEIVED,
            createdAt = Clock.System.now(),
            securityCode = securityCode
        )
        
        val transaction2 = YapeTransaction(
            id = 0,
            transactionId = "DEDUP_002",
            amount = 20.00,
            currency = "PEN",
            senderName = "User 2",
            transactionType = TransactionType.RECEIVED,
            createdAt = Clock.System.now(),
            securityCode = securityCode // Same security code
        )
        
        // When
        repository.insertTransaction(transaction1)
        repository.insertTransaction(transaction2)
        
        // Create new repository instance
        val newRepository = YapeTransactionRepositoryImpl(context)
        
        // Try to insert duplicate again
        val transaction3 = YapeTransaction(
            id = 0,
            transactionId = "DEDUP_003",
            amount = 30.00,
            currency = "PEN",
            senderName = "User 3",
            transactionType = TransactionType.RECEIVED,
            createdAt = Clock.System.now(),
            securityCode = securityCode // Same security code
        )
        
        newRepository.insertTransaction(transaction3)
        
        // Then
        val allTransactions = newRepository.getAllTransactions().first()
        assertEquals(1, allTransactions.size) // Only first transaction should be saved
        assertEquals("DEDUP_001", allTransactions[0].transactionId)
    }
    
    @Test
    fun `test business filtering persistence`() = runBlocking {
        // Given
        val businessA = "Business A"
        val businessB = "Business B"
        
        val transactions = listOf(
            YapeTransaction(
                id = 0,
                transactionId = "BIZ_001",
                amount = 10.00,
                currency = "PEN",
                senderName = "User 1",
                transactionType = TransactionType.RECEIVED,
                businessName = businessA,
                createdAt = Clock.System.now(),
                securityCode = "BIZ_001"
            ),
            YapeTransaction(
                id = 0,
                transactionId = "BIZ_002",
                amount = 20.00,
                currency = "PEN",
                senderName = "User 2",
                transactionType = TransactionType.RECEIVED,
                businessName = businessB,
                createdAt = Clock.System.now(),
                securityCode = "BIZ_002"
            ),
            YapeTransaction(
                id = 0,
                transactionId = "BIZ_003",
                amount = 30.00,
                currency = "PEN",
                senderName = "User 3",
                transactionType = TransactionType.RECEIVED,
                businessName = businessA,
                createdAt = Clock.System.now(),
                securityCode = "BIZ_003"
            )
        )
        
        // When
        transactions.forEach { repository.insertTransaction(it) }
        
        // Create new repository instance
        val newRepository = YapeTransactionRepositoryImpl(context)
        
        // Then
        val businessATransactions = newRepository.getTransactionsByBusiness(businessA).first()
        assertEquals(2, businessATransactions.size)
        assertTrue(businessATransactions.all { it.businessName == businessA })
        
        val businessBTransactions = newRepository.getTransactionsByBusiness(businessB).first()
        assertEquals(1, businessBTransactions.size)
        assertTrue(businessBTransactions.all { it.businessName == businessB })
    }
    
    @Test
    fun `test export functionality with persisted data`() = runBlocking {
        // Given
        val transactions = listOf(
            YapeTransaction(
                id = 0,
                transactionId = "EXPORT_001",
                amount = 100.00,
                currency = "PEN",
                senderName = "Export User 1",
                transactionType = TransactionType.RECEIVED,
                businessName = "Export Business",
                createdAt = Clock.System.now(),
                securityCode = "EXP_001"
            ),
            YapeTransaction(
                id = 0,
                transactionId = "EXPORT_002",
                amount = 200.50,
                currency = "PEN",
                senderName = "Export User 2",
                transactionType = TransactionType.SENT,
                businessName = "Export Business",
                createdAt = Clock.System.now(),
                securityCode = "EXP_002"
            )
        )
        
        // When
        transactions.forEach { repository.insertTransaction(it) }
        
        // Create new repository instance
        val newRepository = YapeTransactionRepositoryImpl(context)
        val exportText = newRepository.exportTransactionsToText()
        
        // Then
        assertTrue(exportText.contains("YAPEHUB - EXPORTACIÓN DE BASE DE DATOS"))
        assertTrue(exportText.contains("Total de transacciones: 2"))
        assertTrue(exportText.contains("EXPORT_001"))
        assertTrue(exportText.contains("EXPORT_002"))
        assertTrue(exportText.contains("Export User 1"))
        assertTrue(exportText.contains("Export User 2"))
        assertTrue(exportText.contains("100.0 PEN"))
        assertTrue(exportText.contains("200.5 PEN"))
    }
}
