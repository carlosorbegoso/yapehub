package org.sysarp.project.test

import org.sysarp.project.service.DebugLogger

/**
 * Ejecutor de tests simple para verificar el sistema YapeChamo
 */
object TestExecutor {
    
    fun runAllTests() {
        DebugLogger.info("🚀 [TEST EXECUTOR] Iniciando ejecución de tests")
        DebugLogger.info("=" * 60)
        
        var passedTests = 0
        var totalTests = 0
        
        try {
            val test = SimpleTransactionTest()
            val duplicateTest = DuplicateDetectionTest()
            
            // Test 1: Lógica de guardado de transacciones
            totalTests++
            try {
                test.`test logica de guardado de transacciones`()
                passedTests++
                DebugLogger.info("✅ [TEST 1/5] Lógica de guardado de transacciones - PASÓ")
            } catch (e: Exception) {
                DebugLogger.error("❌ [TEST 1/5] Lógica de guardado de transacciones - FALLÓ: ${e.message}")
            }
            
            // Test 2: Detección de duplicados
            totalTests++
            try {
                val duplicateResult = duplicateTest.runTest()
                if (duplicateResult.success) {
                    passedTests++
                    DebugLogger.info("✅ [TEST 2/5] Detección de duplicados - PASÓ")
                } else {
                    DebugLogger.error("❌ [TEST 2/5] Detección de duplicados - FALLÓ")
                }
                DebugLogger.info(duplicateResult.message)
            } catch (e: Exception) {
                DebugLogger.error("❌ [TEST 2/5] Detección de duplicados - FALLÓ: ${e.message}")
            }
            
            // Test 3: Simulación de notificaciones de Yape
            totalTests++
            try {
                test.`test simulacion de notificaciones de yape`()
                passedTests++
                DebugLogger.info("✅ [TEST 3/5] Simulación de notificaciones de Yape - PASÓ")
            } catch (e: Exception) {
                DebugLogger.error("❌ [TEST 3/5] Simulación de notificaciones de Yape - FALLÓ: ${e.message}")
            }
            
            // Test 4: Estadísticas del sistema
            totalTests++
            try {
                test.`test estadisticas del sistema`()
                passedTests++
                DebugLogger.info("✅ [TEST 4/5] Estadísticas del sistema - PASÓ")
            } catch (e: Exception) {
                DebugLogger.error("❌ [TEST 4/5] Estadísticas del sistema - FALLÓ: ${e.message}")
            }
            
            // Test 5: Integridad de datos
            totalTests++
            try {
                test.`test integridad de datos`()
                passedTests++
                DebugLogger.info("✅ [TEST 5/5] Integridad de datos - PASÓ")
            } catch (e: Exception) {
                DebugLogger.error("❌ [TEST 5/5] Integridad de datos - FALLÓ: ${e.message}")
            }
            
        } catch (e: Exception) {
            DebugLogger.error("❌ [TEST EXECUTOR] Error crítico durante la ejecución: ${e.message}")
        }
        
        // Resumen final
        DebugLogger.info("=" * 60)
        DebugLogger.info("📊 [TEST EXECUTOR] Resumen de tests ejecutados")
        DebugLogger.info("✅ Tests pasados: $passedTests")
        DebugLogger.info("❌ Tests fallidos: ${totalTests - passedTests}")
        DebugLogger.info("📈 Porcentaje de éxito: ${(passedTests * 100) / totalTests}%")
        
        if (passedTests == totalTests) {
            DebugLogger.info("🎉 [TEST EXECUTOR] ¡Todos los tests pasaron exitosamente!")
            DebugLogger.info("✅ [TEST EXECUTOR] El sistema YapeChamo está funcionando correctamente")
        } else {
            DebugLogger.error("⚠️ [TEST EXECUTOR] Algunos tests fallaron - revisar implementación")
        }
        
        DebugLogger.info("=" * 60)
    }
    
    fun runSpecificTest(testName: String) {
        DebugLogger.info("🎯 [TEST EXECUTOR] Ejecutando test específico: $testName")
        
        try {
            val test = SimpleTransactionTest()
            
            when (testName) {
                "guardado" -> {
                    test.`test logica de guardado de transacciones`()
                    DebugLogger.info("✅ [TEST ESPECÍFICO] Test de guardado completado exitosamente")
                }
                "notificaciones" -> {
                    test.`test simulacion de notificaciones de yape`()
                    DebugLogger.info("✅ [TEST ESPECÍFICO] Test de notificaciones completado exitosamente")
                }
                "estadisticas" -> {
                    test.`test estadisticas del sistema`()
                    DebugLogger.info("✅ [TEST ESPECÍFICO] Test de estadísticas completado exitosamente")
                }
                "integridad" -> {
                    test.`test integridad de datos`()
                    DebugLogger.info("✅ [TEST ESPECÍFICO] Test de integridad completado exitosamente")
                }
                else -> {
                    DebugLogger.error("❌ [TEST ESPECÍFICO] Test no encontrado: $testName")
                    DebugLogger.info("📋 [TEST ESPECÍFICO] Tests disponibles: guardado, notificaciones, estadisticas, integridad")
                }
            }
            
        } catch (e: Exception) {
            DebugLogger.error("❌ [TEST ESPECÍFICO] Error ejecutando test $testName: ${e.message}")
        }
    }
    
    fun runPerformanceTest() {
        DebugLogger.info("⚡ [TEST EXECUTOR] Iniciando test de rendimiento")
        
        val startTime = System.currentTimeMillis()
        
        try {
            // Simular procesamiento de 1000 transacciones
            repeat(1000) { index ->
                val transaction = TransactionData(
                    senderName = "Usuario Performance $index",
                    amount = (index % 1000).toDouble(),
                    securityCode = (index % 100).toString().padStart(3, '0')
                )
                
                // Simular procesamiento
                val processed = processTransaction(transaction)
                if (!processed) {
                    throw Exception("Error procesando transacción $index")
                }
            }
            
            val endTime = System.currentTimeMillis()
            val duration = endTime - startTime
            
            DebugLogger.info("⚡ [TEST EXECUTOR] Test de rendimiento completado")
            DebugLogger.info("⏱️ [TEST EXECUTOR] Tiempo total: ${duration}ms")
            DebugLogger.info("📊 [TEST EXECUTOR] Promedio por transacción: ${duration / 1000.0}ms")
            
            if (duration < 1000) {
                DebugLogger.info("✅ [TEST EXECUTOR] Rendimiento excelente (< 1 segundo para 1000 transacciones)")
            } else if (duration < 5000) {
                DebugLogger.info("✅ [TEST EXECUTOR] Rendimiento bueno (< 5 segundos para 1000 transacciones)")
            } else {
                DebugLogger.warn("⚠️ [TEST EXECUTOR] Rendimiento lento (> 5 segundos para 1000 transacciones)")
            }
            
        } catch (e: Exception) {
            DebugLogger.error("❌ [TEST EXECUTOR] Error en test de rendimiento: ${e.message}")
        }
    }
    
    private fun processTransaction(transaction: TransactionData): Boolean {
        // Simular procesamiento de transacción
        return transaction.senderName.isNotEmpty() && 
               transaction.amount > 0 && 
               transaction.securityCode.isNotEmpty()
    }
    
    // Extensión para repetir strings
    private operator fun String.times(count: Int): String = this.repeat(count)
}

// Clase de datos para el test
data class TransactionData(
    val senderName: String,
    val amount: Double,
    val securityCode: String
)
