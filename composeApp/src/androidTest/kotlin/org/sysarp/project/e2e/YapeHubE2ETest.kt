package org.sysarp.project.e2e

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
class YapeHubE2ETest {
    
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
    fun `test complete yape notification flow`() = runBlocking {
        // Given - Simulate real Yape notifications
        val yapeNotifications = listOf(
            "Confirmación de Pago Carlos Orbegoso L. te envió un pago por S/ 0.1. El cód. de seguridad es: 608",
            "Confirmación de Pago María García te envió un pago por S/ 25.50. El cód. de seguridad es: 789",
            "Confirmación de Pago Enviaste S/ 100.00 a Juan Pérez. El cód. de seguridad es: 123",
            "Confirmación de Pago Ana López te envió un pago por S/ 15.75. El cód. de seguridad es: 456"
        )
        
        // When - Process each notification like the real app would
        yapeNotifications.forEach { notificationText ->
            if (YapeNotificationParser.isYapeNotification(notificationText)) {
                val transaction = YapeNotificationParser.parseYapeNotification(
                    notificationText = notificationText,
                    businessName = "Negocio Principal"
                )
                
                if (transaction != null) {
                    repository.insertTransaction(transaction)
                }
            }
        }
        
        // Then - Verify all transactions were processed correctly
        val allTransactions = repository.getAllTransactions().first()
        assertEquals(4, allTransactions.size)
        
        // Verify received transactions
        val receivedTransactions = allTransactions.filter { it.transactionType == TransactionType.RECEIVED }
        assertEquals(3, receivedTransactions.size)
        
        // Verify sent transactions
        val sentTransactions = allTransactions.filter { it.transactionType == TransactionType.SENT }
        assertEquals(1, sentTransactions.size)
        
        // Verify specific transactions
        val carlosTransaction = allTransactions.find { it.senderName == "Carlos Orbegoso L." }
        assertNotNull(carlosTransaction)
        assertEquals(0.1, carlosTransaction.amount)
        assertEquals("608", carlosTransaction.securityCode)
        
        val mariaTransaction = allTransactions.find { it.senderName == "María García" }
        assertNotNull(mariaTransaction)
        assertEquals(25.50, mariaTransaction.amount)
        assertEquals("789", mariaTransaction.securityCode)
        
        val juanTransaction = allTransactions.find { it.senderName == "Juan Pérez" }
        assertNotNull(juanTransaction)
        assertEquals(100.0, juanTransaction.amount)
        assertEquals(TransactionType.SENT, juanTransaction.transactionType)
        assertEquals("123", juanTransaction.securityCode)
        
        val anaTransaction = allTransactions.find { it.senderName == "Ana López" }
        assertNotNull(anaTransaction)
        assertEquals(15.75, anaTransaction.amount)
        assertEquals("456", anaTransaction.securityCode)
    }
    
    @Test
    fun `test duplicate prevention in real scenario`() = runBlocking {
        // Given - Same notification received multiple times (common in real scenarios)
        val notificationText = "Confirmación de Pago Test User te envió un pago por S/ 50.00. El cód. de seguridad es: 999"
        
        // When - Process the same notification multiple times
        repeat(5) { attempt ->
            if (YapeNotificationParser.isYapeNotification(notificationText)) {
                val transaction = YapeNotificationParser.parseYapeNotification(
                    notificationText = notificationText,
                    businessName = "Test Business"
                )
                
                if (transaction != null) {
                    repository.insertTransaction(transaction)
                }
            }
        }
        
        // Then - Only one transaction should be saved
        val allTransactions = repository.getAllTransactions().first()
        assertEquals(1, allTransactions.size)
        assertEquals("Test User", allTransactions[0].senderName)
        assertEquals(50.0, allTransactions[0].amount)
        assertEquals("999", allTransactions[0].securityCode)
    }
    
    @Test
    fun `test business reporting functionality`() = runBlocking {
        // Given - Transactions for different businesses
        val businessTransactions = listOf(
            Triple("Restaurant A", "Carlos", 25.00, "111"),
            Triple("Restaurant A", "María", 30.50, "222"),
            Triple("Store B", "Juan", 15.75, "333"),
            Triple("Store B", "Ana", 40.00, "444"),
            Triple("Restaurant A", "Pedro", 20.25, "555")
        )
        
        // When - Insert transactions
        businessTransactions.forEach { (business, sender, amount, securityCode) ->
            val transaction = YapeTransaction(
                id = 0,
                transactionId = "BIZ_${securityCode}",
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
        assertEquals(3, restaurantATransactions.size)
        assertTrue(restaurantATransactions.all { it.businessName == "Restaurant A" })
        
        val storeBTransactions = repository.getTransactionsByBusiness("Store B").first()
        assertEquals(2, storeBTransactions.size)
        assertTrue(storeBTransactions.all { it.businessName == "Store B" })
        
        // Verify total amounts
        val restaurantATotal = restaurantATransactions.sumOf { it.amount }
        assertEquals(75.75, restaurantATotal) // 25.00 + 30.50 + 20.25
        
        val storeBTotal = storeBTransactions.sumOf { it.amount }
        assertEquals(55.75, storeBTotal) // 15.75 + 40.00
    }
    
    @Test
    fun `test export functionality with real data`() = runBlocking {
        // Given - Realistic transaction data
        val transactions = listOf(
            YapeTransaction(
                id = 0,
                transactionId = "REAL_001",
                amount = 12.50,
                currency = "PEN",
                senderName = "Cliente Frecuente",
                senderPhone = "999888777",
                message = "Pago por almuerzo",
                transactionType = TransactionType.RECEIVED,
                businessName = "Restaurant El Buen Sabor",
                createdAt = Clock.System.now(),
                securityCode = "REAL_001"
            ),
            YapeTransaction(
                id = 0,
                transactionId = "REAL_002",
                amount = 8.00,
                currency = "PEN",
                senderName = "María González",
                transactionType = TransactionType.RECEIVED,
                businessName = "Restaurant El Buen Sabor",
                createdAt = Clock.System.now(),
                securityCode = "REAL_002"
            )
        )
        
        // When
        transactions.forEach { repository.insertTransaction(it) }
        val exportText = repository.exportTransactionsToText()
        
        // Then
        assertTrue(exportText.contains("YAPEHUB - EXPORTACIÓN DE BASE DE DATOS"))
        assertTrue(exportText.contains("Total de transacciones: 2"))
        assertTrue(exportText.contains("REAL_001"))
        assertTrue(exportText.contains("REAL_002"))
        assertTrue(exportText.contains("Cliente Frecuente"))
        assertTrue(exportText.contains("María González"))
        assertTrue(exportText.contains("12.5 PEN"))
        assertTrue(exportText.contains("8.0 PEN"))
        assertTrue(exportText.contains("Restaurant El Buen Sabor"))
        assertTrue(exportText.contains("Pago por almuerzo"))
        assertTrue(exportText.contains("999888777"))
    }
}
