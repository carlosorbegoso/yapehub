package org.sysarp.project.repository

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
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
class YapeTransactionRepositoryTest {
    
    private lateinit var repository: YapeTransactionRepository
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
    fun `test insert transaction should save to database`() = runBlocking {
        // Given
        val transaction = YapeTransaction(
            id = 0,
            transactionId = "TEST_001",
            amount = 10.50,
            currency = "PEN",
            senderName = "Test User",
            senderPhone = "999999999",
            message = "Test payment",
            transactionType = TransactionType.RECEIVED,
            businessName = "Test Business",
            createdAt = Clock.System.now(),
            processedAt = null,
            isProcessed = false,
            rawNotification = "Test notification",
            securityCode = "123456"
        )
        
        // When
        repository.insertTransaction(transaction)
        
        // Then
        val allTransactions = repository.getAllTransactions().first()
        assertEquals(1, allTransactions.size)
        assertEquals("TEST_001", allTransactions[0].transactionId)
        assertEquals(10.50, allTransactions[0].amount)
        assertEquals("Test User", allTransactions[0].senderName)
    }
    
    @Test
    fun `test duplicate transaction should be prevented`() = runBlocking {
        // Given
        val transaction1 = YapeTransaction(
            id = 0,
            transactionId = "TEST_002",
            amount = 5.00,
            currency = "PEN",
            senderName = "User 1",
            transactionType = TransactionType.RECEIVED,
            createdAt = Clock.System.now(),
            securityCode = "111111"
        )
        
        val transaction2 = YapeTransaction(
            id = 0,
            transactionId = "TEST_003",
            amount = 5.00,
            currency = "PEN",
            senderName = "User 2",
            transactionType = TransactionType.RECEIVED,
            createdAt = Clock.System.now(),
            securityCode = "111111" // Mismo código de seguridad
        )
        
        // When
        repository.insertTransaction(transaction1)
        repository.insertTransaction(transaction2)
        
        // Then
        val allTransactions = repository.getAllTransactions().first()
        assertEquals(1, allTransactions.size) // Solo la primera transacción
    }
    
    @Test
    fun `test get transactions by business`() = runBlocking {
        // Given
        val transaction1 = YapeTransaction(
            id = 0,
            transactionId = "TEST_003",
            amount = 10.00,
            currency = "PEN",
            senderName = "User 1",
            transactionType = TransactionType.RECEIVED,
            businessName = "Business A",
            createdAt = Clock.System.now(),
            securityCode = "333333"
        )
        
        val transaction2 = YapeTransaction(
            id = 0,
            transactionId = "TEST_004",
            amount = 20.00,
            currency = "PEN",
            senderName = "User 2",
            transactionType = TransactionType.RECEIVED,
            businessName = "Business B",
            createdAt = Clock.System.now(),
            securityCode = "444444"
        )
        
        // When
        repository.insertTransaction(transaction1)
        repository.insertTransaction(transaction2)
        
        // Then
        val businessATransactions = repository.getTransactionsByBusiness("Business A").first()
        assertEquals(1, businessATransactions.size)
        assertEquals("Business A", businessATransactions[0].businessName)
    }
    
    @Test
    fun `test update transaction processed status`() = runBlocking {
        // Given
        val transaction = YapeTransaction(
            id = 0,
            transactionId = "TEST_005",
            amount = 15.00,
            currency = "PEN",
            senderName = "User Test",
            transactionType = TransactionType.RECEIVED,
            createdAt = Clock.System.now(),
            isProcessed = false,
            securityCode = "555555"
        )
        
        repository.insertTransaction(transaction)
        val insertedTransaction = repository.getAllTransactions().first()[0]
        
        // When
        repository.updateTransactionProcessed(insertedTransaction.id)
        
        // Then
        val updatedTransactions = repository.getAllTransactions().first()
        assertTrue(updatedTransactions[0].isProcessed)
    }
    
    @Test
    fun `test export transactions to text`() = runBlocking {
        // Given
        val transaction = YapeTransaction(
            id = 0,
            transactionId = "TEST_006",
            amount = 25.00,
            currency = "PEN",
            senderName = "Export Test User",
            transactionType = TransactionType.RECEIVED,
            businessName = "Export Business",
            createdAt = Clock.System.now(),
            securityCode = "666666"
        )
        
        repository.insertTransaction(transaction)
        
        // When
        val exportText = repository.exportTransactionsToText()
        
        // Then
        assertTrue(exportText.contains("YAPEHUB - EXPORTACIÓN DE BASE DE DATOS"))
        assertTrue(exportText.contains("TEST_006"))
        assertTrue(exportText.contains("Export Test User"))
        assertTrue(exportText.contains("25.0 PEN"))
    }
}
