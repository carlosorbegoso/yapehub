package org.sysarp.project.test.integration

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.sysarp.project.data.TransactionType
import org.sysarp.project.data.YapeTransaction
import kotlinx.datetime.Clock
import kotlin.test.*

/**
 * Test básico para verificar la sincronización del StateFlow
 * Este test verifica que cuando se inserta una transacción en el repositorio,
 * el StateFlow emita correctamente los cambios
 */
class BasicSyncTest {

    @Test
    fun `test stateflow emits changes when transaction is inserted`() {
        // Crear repositorio mock simple
        val repository = BasicMockRepository()
        
        // Verificar que inicialmente no hay transacciones
        val initialTransactions = repository.getAllTransactions().value
        assertEquals(0, initialTransactions.size, "Inicialmente no debería haber transacciones")

        // Crear transacción de prueba
        val testTransaction = YapeTransaction(
            id = 1L,
            transactionId = "test_tx_001",
            amount = 10.50,
            currency = "PEN",
            senderName = "Test Sender",
            senderPhone = "+51987654321",
            message = "Test payment",
            transactionType = TransactionType.RECEIVED,
            businessName = "Test Business",
            createdAt = Clock.System.now(),
            processedAt = null,
            isProcessed = false,
            rawNotification = "Test notification",
            securityCode = "123"
        )

        // Insertar transacción en el repositorio
        repository.insertTransaction(testTransaction)

        // Verificar que el StateFlow emitió el cambio
        val updatedTransactions = repository.getAllTransactions().value
        assertEquals(1, updatedTransactions.size, "El repositorio debería tener 1 transacción después de la inserción")
        
        val receivedTransaction = updatedTransactions.first()
        assertEquals(testTransaction.id, receivedTransaction.id, "El ID de la transacción debería coincidir")
        assertEquals(testTransaction.amount, receivedTransaction.amount, "El monto debería coincidir")
        assertEquals(testTransaction.senderName, receivedTransaction.senderName, "El nombre del remitente debería coincidir")
        assertEquals(testTransaction.securityCode, receivedTransaction.securityCode, "El código de seguridad debería coincidir")
    }

    @Test
    fun `test stateflow emits changes for multiple transactions`() {
        val repository = BasicMockRepository()

        // Crear múltiples transacciones
        val transactions = listOf(
            YapeTransaction(
                id = 1L,
                transactionId = "test_tx_001",
                amount = 10.50,
                currency = "PEN",
                senderName = "Sender 1",
                transactionType = TransactionType.RECEIVED,
                createdAt = Clock.System.now(),
                securityCode = "123"
            ),
            YapeTransaction(
                id = 2L,
                transactionId = "test_tx_002",
                amount = 25.75,
                currency = "PEN",
                senderName = "Sender 2",
                transactionType = TransactionType.RECEIVED,
                createdAt = Clock.System.now(),
                securityCode = "456"
            ),
            YapeTransaction(
                id = 3L,
                transactionId = "test_tx_003",
                amount = 5.00,
                currency = "PEN",
                senderName = "Sender 3",
                transactionType = TransactionType.RECEIVED,
                createdAt = Clock.System.now(),
                securityCode = "789"
            )
        )

        // Insertar todas las transacciones
        transactions.forEach { transaction ->
            repository.insertTransaction(transaction)
        }

        // Verificar que el repositorio recibió todas las transacciones
        val repositoryTransactions = repository.getAllTransactions().value
        assertEquals(3, repositoryTransactions.size, "El repositorio debería tener 3 transacciones")

        // Verificar que todas las transacciones están presentes
        val transactionIds = repositoryTransactions.map { it.id }.sorted()
        assertEquals(listOf(1L, 2L, 3L), transactionIds, "Todos los IDs de transacciones deberían estar presentes")

        // Verificar que los montos son correctos
        val amounts = repositoryTransactions.map { it.amount }.sorted()
        assertEquals(listOf(5.0, 10.5, 25.75), amounts, "Todos los montos deberían estar presentes")
    }

    @Test
    fun `test duplicate security codes are rejected`() {
        val repository = BasicMockRepository()

        // Crear primera transacción
        val firstTransaction = YapeTransaction(
            id = 1L,
            transactionId = "test_tx_001",
            amount = 10.50,
            currency = "PEN",
            senderName = "Sender 1",
            transactionType = TransactionType.RECEIVED,
            createdAt = Clock.System.now(),
            securityCode = "123"
        )

        // Crear segunda transacción con el mismo código de seguridad
        val duplicateTransaction = YapeTransaction(
            id = 2L,
            transactionId = "test_tx_002",
            amount = 25.75,
            currency = "PEN",
            senderName = "Sender 2",
            transactionType = TransactionType.RECEIVED,
            createdAt = Clock.System.now(),
            securityCode = "123" // Mismo código de seguridad
        )

        // Insertar primera transacción
        repository.insertTransaction(firstTransaction)

        // Verificar que se insertó la primera transacción
        val afterFirst = repository.getAllTransactions().value
        assertEquals(1, afterFirst.size, "Debería haber 1 transacción después de la primera inserción")

        // Intentar insertar transacción duplicada
        repository.insertTransaction(duplicateTransaction)

        // Verificar que no se insertó la transacción duplicada
        val afterDuplicate = repository.getAllTransactions().value
        assertEquals(1, afterDuplicate.size, "No debería insertarse la transacción duplicada")
        assertEquals("Sender 1", afterDuplicate.first().senderName, "Debería mantener la primera transacción")
    }

    @Test
    fun `test stateflow emits immediately`() {
        val repository = BasicMockRepository()

        // Crear transacción
        val testTransaction = YapeTransaction(
            id = 1L,
            transactionId = "test_tx_001",
            amount = 7.50,
            currency = "PEN",
            senderName = "Immediate Test",
            transactionType = TransactionType.RECEIVED,
            createdAt = Clock.System.now(),
            securityCode = "777"
        )

        // Insertar transacción
        repository.insertTransaction(testTransaction)

        // Verificar inmediatamente que el StateFlow emitió el cambio
        val transactions = repository.getAllTransactions().value
        assertEquals(1, transactions.size, "El StateFlow debería emitir inmediatamente el cambio")
        assertEquals("Immediate Test", transactions.first().senderName, "La transacción debería estar disponible inmediatamente")
    }
}

// Mock repository básico para testing
class BasicMockRepository {
    private val _transactions = MutableStateFlow<List<YapeTransaction>>(emptyList())
    private val processedSecurityCodes = mutableSetOf<String>()

    fun insertTransaction(transaction: YapeTransaction) {
        val securityCode = transaction.securityCode
        if (securityCode != null && processedSecurityCodes.contains(securityCode)) {
            return // Rechazar duplicados
        }

        val currentList = _transactions.value.toMutableList()
        currentList.add(transaction)
        _transactions.value = currentList

        if (securityCode != null) {
            processedSecurityCodes.add(securityCode)
        }
    }

    fun getAllTransactions(): StateFlow<List<YapeTransaction>> = _transactions.asStateFlow()
}
