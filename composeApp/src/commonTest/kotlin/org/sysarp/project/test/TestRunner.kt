package org.sysarp.project.test

import org.sysarp.project.service.DebugLogger
import kotlin.test.*

/**
 * Runner principal para ejecutar todos los tests del sistema YapeChamo
 */
class TestRunner {

    @Test
    fun `ejecutar todos los tests del sistema`() {
        DebugLogger.info("🚀 [TEST RUNNER] Iniciando suite completa de tests")
        DebugLogger.info("=" * 60)
        
        try {
            // Ejecutar tests de almacenamiento
            DebugLogger.info("📦 [TEST RUNNER] Ejecutando tests de almacenamiento...")
            runStorageTests()
            
            // Ejecutar tests de comportamiento real
            DebugLogger.info("🌍 [TEST RUNNER] Ejecutando tests de comportamiento real...")
            runRealWorldTests()
            
            // Ejecutar tests de integración del ViewModel
            DebugLogger.info("🔧 [TEST RUNNER] Ejecutando tests de integración del ViewModel...")
            runViewModelTests()
            
            DebugLogger.info("=" * 60)
            DebugLogger.info("✅ [TEST RUNNER] Todos los tests completados exitosamente")
            DebugLogger.info("🎉 [TEST RUNNER] Sistema YapeChamo verificado completamente")
            
        } catch (e: Exception) {
            DebugLogger.error("❌ [TEST RUNNER] Error durante la ejecución de tests: ${e.message}")
            fail("Error en la suite de tests: ${e.message}")
        }
    }

    private fun runStorageTests() {
        val test = TransactionStorageTest()
        
        try {
            test.setup()
            
            // Ejecutar tests individuales
            test.`test guardar transaccion unica`()
            test.`test guardar transacciones duplicadas`()
            test.`test filtrado de transacciones unicas`()
            test.`test integridad de base de datos`()
            test.`test estadisticas de transacciones`()
            test.`test manejo de transacciones sin codigo de seguridad`()
            
            test.cleanup()
            DebugLogger.info("✅ [STORAGE TESTS] Tests de almacenamiento completados")
            
        } catch (e: Exception) {
            DebugLogger.error("❌ [STORAGE TESTS] Error en tests de almacenamiento: ${e.message}")
            throw e
        }
    }

    private fun runRealWorldTests() {
        val test = RealWorldTransactionTest()
        
        try {
            test.setup()
            
            // Ejecutar tests individuales
            test.`test simulacion de notificaciones reales de yape`()
            test.`test escenario de alta frecuencia de transacciones`()
            test.`test verificacion de integridad completa`()
            
            test.cleanup()
            DebugLogger.info("✅ [REAL WORLD TESTS] Tests de comportamiento real completados")
            
        } catch (e: Exception) {
            DebugLogger.error("❌ [REAL WORLD TESTS] Error en tests de comportamiento real: ${e.message}")
            throw e
        }
    }

    private fun runViewModelTests() {
        val test = ViewModelIntegrationTest()
        
        try {
            test.setup()
            
            // Ejecutar tests individuales
            test.`test viewmodel actualiza correctamente con transacciones unicas`()
            test.`test viewmodel maneja duplicados correctamente`()
            test.`test exportacion de base de datos`()
            test.`test verificacion de integridad del viewmodel`()
            test.`test simulacion de usuario admin`()
            test.`test simulacion de usuario vendor`()
            
            test.cleanup()
            DebugLogger.info("✅ [VIEWMODEL TESTS] Tests de integración del ViewModel completados")
            
        } catch (e: Exception) {
            DebugLogger.error("❌ [VIEWMODEL TESTS] Error en tests del ViewModel: ${e.message}")
            throw e
        }
    }

    @Test
    fun `test de rendimiento del sistema`() {
        DebugLogger.info("⚡ [PERFORMANCE TEST] Iniciando test de rendimiento")
        
        val startTime = System.currentTimeMillis()
        val mockRepository = MockYapeTransactionRepository()
        
        try {
            // Simular 1000 transacciones
            repeat(1000) { index ->
                val transaction = createPerformanceTestTransaction(index)
                mockRepository.insertTransaction(transaction)
            }
            
            val endTime = System.currentTimeMillis()
            val duration = endTime - startTime
            
            DebugLogger.info("⚡ [PERFORMANCE TEST] 1000 transacciones procesadas en ${duration}ms")
            DebugLogger.info("⚡ [PERFORMANCE TEST] Promedio: ${duration / 1000.0}ms por transacción")
            
            // Verificar que el rendimiento es aceptable (menos de 1 segundo para 1000 transacciones)
            assertTrue(duration < 1000, "El sistema debe procesar 1000 transacciones en menos de 1 segundo")
            
            DebugLogger.info("✅ [PERFORMANCE TEST] Test de rendimiento completado exitosamente")
            
        } catch (e: Exception) {
            DebugLogger.error("❌ [PERFORMANCE TEST] Error en test de rendimiento: ${e.message}")
            throw e
        }
    }

    @Test
    fun `test de memoria del sistema`() {
        DebugLogger.info("🧠 [MEMORY TEST] Iniciando test de memoria")
        
        val mockRepository = MockYapeTransactionRepository()
        val initialMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()
        
        try {
            // Crear muchas transacciones para probar el uso de memoria
            repeat(10000) { index ->
                val transaction = createMemoryTestTransaction(index)
                mockRepository.insertTransaction(transaction)
            }
            
            // Forzar garbage collection
            System.gc()
            Thread.sleep(100)
            
            val finalMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()
            val memoryUsed = finalMemory - initialMemory
            
            DebugLogger.info("🧠 [MEMORY TEST] Memoria inicial: ${initialMemory / 1024 / 1024}MB")
            DebugLogger.info("🧠 [MEMORY TEST] Memoria final: ${finalMemory / 1024 / 1024}MB")
            DebugLogger.info("🧠 [MEMORY TEST] Memoria usada: ${memoryUsed / 1024 / 1024}MB")
            
            // Verificar que el uso de memoria es razonable (menos de 100MB para 10000 transacciones)
            assertTrue(memoryUsed < 100 * 1024 * 1024, "El sistema no debe usar más de 100MB para 10000 transacciones")
            
            DebugLogger.info("✅ [MEMORY TEST] Test de memoria completado exitosamente")
            
        } catch (e: Exception) {
            DebugLogger.error("❌ [MEMORY TEST] Error en test de memoria: ${e.message}")
            throw e
        }
    }

    private fun createPerformanceTestTransaction(index: Int): YapeTransaction {
        return YapeTransaction(
            id = 0L,
            transactionId = "",
            amount = (index % 1000).toDouble(),
            currency = "PEN",
            senderName = "Usuario Performance $index",
            senderPhone = "+51999999999",
            message = "Test performance transaction $index",
            transactionType = org.sysarp.project.data.TransactionType.RECEIVED,
            businessName = "Negocio Performance",
            createdAt = kotlinx.datetime.Clock.System.now(),
            processedAt = null,
            isProcessed = false,
            rawNotification = "Test notification $index",
            securityCode = (index % 100).toString().padStart(3, '0')
        )
    }

    private fun createMemoryTestTransaction(index: Int): YapeTransaction {
        return YapeTransaction(
            id = 0L,
            transactionId = "",
            amount = (index % 1000).toDouble(),
            currency = "PEN",
            senderName = "Usuario Memoria $index",
            senderPhone = "+51999999999",
            message = "Test memory transaction $index".repeat(10), // Mensaje más largo
            transactionType = org.sysarp.project.data.TransactionType.RECEIVED,
            businessName = "Negocio Memoria $index",
            createdAt = kotlinx.datetime.Clock.System.now(),
            processedAt = null,
            isProcessed = false,
            rawNotification = "Test notification $index".repeat(5), // Notificación más larga
            securityCode = (index % 1000).toString().padStart(4, '0')
        )
    }
}

/**
 * Extensión para repetir strings
 */
private operator fun String.times(count: Int): String = this.repeat(count)
