package org.sysarp.project.test

import org.sysarp.project.service.DebugLogger

/**
 * Test principal que se puede ejecutar para verificar el sistema YapeChamo
 */
fun main() {
    DebugLogger.info("🚀 [MAIN TEST] Iniciando verificación del sistema YapeChamo")
    DebugLogger.info("=" * 80)
    
    try {
        // Ejecutar todos los tests
        TestExecutor.runAllTests()
        
        // Ejecutar test de rendimiento
        DebugLogger.info("")
        TestExecutor.runPerformanceTest()
        
        DebugLogger.info("")
        DebugLogger.info("🎉 [MAIN TEST] Verificación completa del sistema finalizada")
        DebugLogger.info("✅ [MAIN TEST] El sistema YapeChamo está listo para usar")
        
    } catch (e: Exception) {
        DebugLogger.error("❌ [MAIN TEST] Error crítico durante la verificación: ${e.message}")
        DebugLogger.error("🔧 [MAIN TEST] Stack trace: ${e.stackTraceToString()}")
    }
    
    DebugLogger.info("=" * 80)
}

// Extensión para repetir strings
private operator fun String.times(count: Int): String = this.repeat(count)
