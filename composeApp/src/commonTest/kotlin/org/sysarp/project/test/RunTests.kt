package org.sysarp.project.test

import org.sysarp.project.service.DebugLogger

/**
 * Script simple para ejecutar las pruebas unitarias
 */
fun main() {
    println("🚀 [YAPECHAMO TESTS] Iniciando pruebas unitarias...")
    println("=" * 60)
    
    try {
        // Ejecutar todas las pruebas
        TestExecutor.runAllTests()
        
        println("=" * 60)
        println("🎉 [YAPECHAMO TESTS] Pruebas completadas")
        
    } catch (e: Exception) {
        println("❌ [YAPECHAMO TESTS] Error durante la ejecución: ${e.message}")
        e.printStackTrace()
    }
}
