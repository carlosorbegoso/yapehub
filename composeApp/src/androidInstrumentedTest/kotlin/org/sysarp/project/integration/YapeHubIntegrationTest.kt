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
import org.sysarp.project.service.YapeNotificationParser
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
class YapeHubIntegrationTest {
    
    private lateinit var repository: YapeTransactionRepositoryImpl
    private lateinit var context: Context
    
    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        repository = YapeTransactionRepositoryImpl(context)
    }
    
    @After
    fun cleanup() {
        context.deleteDatabase("yape_transactions.db")
    }
    
    @Test
    fun testCompleteYapeFlow() = runBlocking {
        // Given - Simulate real Yape notification
        val yapeNotification = "Confirmación de Pago Carlos Orbegoso L. te envió un pago por S/ 25.50. El cód. de seguridad es: 123"
        
        // When - Process notification like the real app
        val isYapeNotification = YapeNotificationParser.isYapeNotification(yapeNotification)
        assertTrue(isYapeNotification, "Should detect as Yape notification")
        
        val transaction = YapeNotificationParser.parseYapeNotification(
            notificationText = yapeNotification,
            businessName = "Test Business"
        )
        
        assertNotNull(transaction, "Transaction should be parsed successfully")
        
        // Insert into repository
        repository.insertTransaction(transaction)
        
        // Then - Verify transaction was saved
        val allTransactions = repository.getAllTransactions().first()
        assertEquals(1, allTransactions.size)
        
        val savedTransaction = allTransactions[0]
        assertEquals("Carlos Orbegoso L.", savedTransaction.senderName)
        assertEquals(25.50, savedTransaction.amount)
        assertEquals("PEN", savedTransaction.currency)
        assertEquals(TransactionType.RECEIVED, savedTransaction.transactionType)
        assertEquals("123", savedTransaction.securityCode)
        assertEquals("Test Business", savedTransaction.businessName)
        
        // Verify export functionality
        val exportText = repository.exportTransactionsToText()
        assertTrue(exportText.contains("YAPEHUB - EXPORTACIÓN DE BASE DE DATOS"))
        assertTrue(exportText.contains("Carlos Orbegoso L."))
        assertTrue(exportText.contains("25.5 PEN"))
        assertTrue(exportText.contains("123"))
    }
    
    @Test
    fun testDuplicatePrevention() = runBlocking {
        // Given - Same notification multiple times
        val notification = "Confirmación de Pago Test User te envió un pago por S/ 10.00. El cód. de seguridad es: 999"
        
        // When - Process same notification multiple times
        repeat(3) {
            val transaction = YapeNotificationParser.parseYapeNotification(
                notificationText = notification,
                businessName = "Test Business"
            )
            if (transaction != null) {
                repository.insertTransaction(transaction)
            }
        }
        
        // Then - Only one transaction should be saved
        val allTransactions = repository.getAllTransactions().first()
        assertEquals(1, allTransactions.size)
        assertEquals("Test User", allTransactions[0].senderName)
        assertEquals("999", allTransactions[0].securityCode)
    }
    
    @Test
    fun testBusinessFiltering() = runBlocking {
        // Given - Transactions for different businesses
        val transactions = listOf(
            Triple("Restaurant A", "Carlos", 15.00, "111"),
            Triple("Restaurant A", "María", 20.00, "222"),
            Triple("Store B", "Juan", 10.00, "333")
        )
        
        // When - Insert transactions
        transactions.forEach { (business, sender, amount, securityCode) ->
            val transaction = YapeTransaction(
                id = 0,
                transactionId = "TEST_${securityCode}",
                amount = amount,
                currency = "PEN",
                senderName = sender,
                transactionType = TransactionType.RECEIVED,
                businessName = business,
                createdAt = Clock.System.now(),
                securityCode = securityCode
            )
            repository.insertTransaction(transaction)
        }
        
        // Then - Verify business filtering
        val restaurantATransactions = repository.getTransactionsByBusiness("Restaurant A").first()
        assertEquals(2, restaurantATransactions.size)
        assertTrue(restaurantATransactions.all { it.businessName == "Restaurant A" })
        
        val storeBTransactions = repository.getTransactionsByBusiness("Store B").first()
        assertEquals(1, storeBTransactions.size)
        assertTrue(storeBTransactions.all { it.businessName == "Store B" })
    }
}
